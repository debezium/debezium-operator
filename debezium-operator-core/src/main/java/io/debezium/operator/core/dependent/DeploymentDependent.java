/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core.dependent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.debezium.operator.api.model.CommonLabels;
import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.api.model.runtime.RuntimeEnvironment;
import io.debezium.operator.api.model.runtime.jmx.JmxConfig;
import io.debezium.operator.api.model.runtime.metrics.Metrics;
import io.debezium.operator.api.model.runtime.storage.RuntimeStorage;
import io.debezium.operator.api.model.runtime.templates.ContainerTemplate;
import io.debezium.operator.api.model.runtime.templates.PodTemplate;
import io.debezium.operator.commons.util.StringUtils;
import io.debezium.operator.core.ServerImageProvider;
import io.fabric8.kubernetes.api.model.ConfigMapVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.EmptyDirVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.HTTPGetActionBuilder;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.LabelSelectorBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpecBuilder;
import io.fabric8.kubernetes.api.model.ProbeBuilder;
import io.fabric8.kubernetes.api.model.SecretVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpecBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;

public class DeploymentDependent extends CRUDKubernetesDependentResource<Deployment, DebeziumServer> {

    public static final String DEFAULT_IMAGE = "quay.io/debezium/server";
    public static final String CONFIG_VOLUME_NAME = "ds-config";
    public static final String CONFIG_FILE_NAME = "application.properties";
    public static final String CONFIG_DIR_PATH = "/debezium/conf";
    public static final String CONFIG_FILE_PATH = CONFIG_DIR_PATH + "/" + CONFIG_FILE_NAME;
    public static final String METRICS_VOLUME_NAME = "ds-metrics";
    public static final String METRICS_DIR_PATH = "/debezium/metrics";
    public static final String METRICS_FILE_PATH = METRICS_DIR_PATH + "/%s";
    public static final String DATA_VOLUME_NAME = "ds-data";
    public static final String DATA_VOLUME_PATH = "/debezium/data";
    public static final String JMX_CONFIG_VOLUME_NAME = "ds-jmx-config";
    public static final String JMX_CONFIG_VOLUME_INIT_NAME = "ds-jmx-config-init";
    public static final String JMX_CONFIG_VOLUME_PATH = CONFIG_DIR_PATH + "/jmx";
    public static final String JMX_CONFIG_VOLUME_INIT_PATH = "/jmx";
    public static final String EXTERNAL_VOLUME_PATH = "/debezium/external/%s";
    public static final int DEFAULT_HTTP_PORT = 8080;
    public static final int DEFAULT_JMX_EXPORTER_METRICS_PORT = 9090;
    public static final String JMX_EXPORTER_METRICS_PORT_NAME = "metrics-jmx";
    private static final String CONFIG_MD5_ANNOTATION = "debezium.io/server-config-md5";

    @ConfigProperty(name = "debezium.server.image.name", defaultValue = DEFAULT_IMAGE)
    String defaultImage;

    @Inject
    ServerImageProvider imageProvider;

    @Inject
    Logger logger;

    public DeploymentDependent() {
        super(Deployment.class);
    }

    @Override
    protected Deployment desired(DebeziumServer primary, Context<DebeziumServer> context) {
        var runtime = primary.getSpec().getRuntime();
        var storage = runtime.getStorage();
        var templates = runtime.getTemplates();
        var name = primary.getMetadata().getName();
        var labels = CommonLabels.serverComponent(name).getMap();
        var primaryLabels = primary.getMetadata().getLabels();
        var annotations = Map.of(CONFIG_MD5_ANNOTATION, primary.asConfiguration().md5Sum());
        var replicas = desiredReplicas(primary);

        var sa = ServiceAccountDependent.serviceAccountNameFor(primary);

        var desiredContainer = desiredServerContainer(primary);

        var pod = new PodTemplateSpecBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withLabels(primaryLabels)
                        .addToLabels(labels)
                        .withAnnotations(annotations)
                        .build())
                .withSpec(new PodSpecBuilder()
                        .withServiceAccountName(sa)
                        .addToVolumes(new VolumeBuilder()
                                .withName(CONFIG_VOLUME_NAME)
                                .withConfigMap(new ConfigMapVolumeSourceBuilder()
                                        .withName(name)
                                        .build())
                                .build())
                        .addToContainers(desiredContainer)
                        .build())
                .build();

        addTemplateConfigurationToPod(templates.getPod(), pod);
        addStorageVolumesToPod(primary, storage, pod);
        addJmxConfigurationToPod(primary, pod);
        addMetricConfigurationToPod(primary, pod);

        return new DeploymentBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withNamespace(primary.getMetadata().getNamespace())
                        .withName(name)
                        .withLabels(primaryLabels)
                        .addToLabels(labels)
                        .withAnnotations(annotations)
                        .build())
                .withSpec(new DeploymentSpecBuilder()
                        .withReplicas(replicas)
                        .withSelector(new LabelSelectorBuilder()
                                .addToMatchLabels(labels)
                                .build())
                        .withTemplate(pod)
                        .build())
                .build();
    }

    private int desiredReplicas(DebeziumServer primary) {
        return primary.isStopped() ? 0 : 1;
    }

    private void addMetricConfigurationToPod(DebeziumServer primary, PodTemplateSpec pod) {
        var metrics = primary.getSpec().getRuntime().getMetrics();
        var jmxExporter = metrics.getJmxExporter();
        var volumes = pod.getSpec().getVolumes();

        if (!jmxExporter.isEnabled()) {
            return;
        }

        // Add config map volume if required
        var configMapRef = jmxExporter.getConfigFrom();
        if (configMapRef == null) {
            return;
        }

        var metricsVolume = new VolumeBuilder()
                .withName(METRICS_VOLUME_NAME)
                .withConfigMap(new ConfigMapVolumeSourceBuilder()
                        .withName(configMapRef.getName())
                        .build())
                .build();

        volumes.add(metricsVolume);
    }

    /**
     * Applies pod template configuration to pod if required
     *
     * @param template pod template configuration
     * @param pod      actual pod template spec
     */
    private void addTemplateConfigurationToPod(PodTemplate template, PodTemplateSpec pod) {
        var templateMeta = template.getMetadata();
        var podSpec = pod.getSpec();
        var podMeta = pod.getMetadata();

        podSpec.setSecurityContext(template.getSecurityContext());
        podSpec.setAffinity(template.getAffinity());
        podSpec.setImagePullSecrets(template.getImagePullSecrets());
        podMeta.getLabels().putAll(templateMeta.getLabels());
        podMeta.getAnnotations().putAll(templateMeta.getAnnotations());
    }

    /**
     * Adds storage volume definitions to pod if required
     *
     * @param primary
     * @param storage runtime storage configuration
     * @param pod     actual pod template spec
     */
    private void addStorageVolumesToPod(DebeziumServer primary, RuntimeStorage storage, PodTemplateSpec pod) {
        var volumes = pod.getSpec().getVolumes();

        // add data volume
        desiredDataVolume(primary)
                .ifPresent(volumes::add);

        // add external volumes
        volumes.addAll(storage.getExternal());
    }

    private Optional<Volume> desiredDataVolume(DebeziumServer primary) {
        var builder = new VolumeBuilder().withName(DATA_VOLUME_NAME);
        var dataStorage = primary.getSpec().getRuntime().getStorage().getData();

        switch (dataStorage.getType()) {
            case EPHEMERAL -> builder
                    .withEmptyDir(new EmptyDirVolumeSourceBuilder()
                            .build());
            case PERSISTENT -> builder
                    .withPersistentVolumeClaim(new PersistentVolumeClaimVolumeSourceBuilder()
                            .withClaimName(PvcDependent.pvcNameFor(primary))
                            .build());
        }
        var volume = builder.build();
        return Optional.of(volume);
    }

    private Optional<VolumeMount> desiredDataVolumeMount() {
        var volumeMount = new VolumeMountBuilder()
                .withName(DATA_VOLUME_NAME)
                .withMountPath(DATA_VOLUME_PATH)
                .build();

        return Optional.of(volumeMount);
    }

    /**
     * Adds JMX configuration to pod if required
     *
     * @param primary primary resource
     * @param pod     target pod
     */
    private void addJmxConfigurationToPod(DebeziumServer primary, PodTemplateSpec pod) {
        var jmx = primary.getSpec().getRuntime().getJmx();
        var auth = jmx.getAuthentication();

        if (!auth.isEnabled()) {
            return;
        }

        var volumes = pod.getSpec().getVolumes();
        var initContainers = pod.getSpec().getInitContainers();

        // Add JMX volumes to pod
        var jmxInitVolume = new VolumeBuilder()
                .withName(JMX_CONFIG_VOLUME_INIT_NAME)
                .withSecret(new SecretVolumeSourceBuilder()
                        .withSecretName(auth.getSecret())
                        .build())
                .build();

        var jmxConfigVolume = new VolumeBuilder()
                .withName(JMX_CONFIG_VOLUME_NAME)
                .withEmptyDir(new EmptyDirVolumeSourceBuilder().build())
                .build();

        volumes.add(jmxInitVolume);
        volumes.add(jmxConfigVolume);

        // Add JMX init container
        var image = imageProvider.getImage(primary);
        var container = desiredJmxInitContainer(jmx, image);
        container.ifPresent(initContainers::add);
    }

    private void addTemplateConfigurationToContainer(ContainerTemplate template, Container container) {
        container.setSecurityContext(template.getSecurityContext());
        container.setResources(template.getResources());
    }

    /**
     * Creates desired server container
     *
     * @param primary primary CR
     * @return desired server container
     */
    private Container desiredServerContainer(DebeziumServer primary) {
        var quarkus = primary.getSpec().getQuarkus();
        var runtime = primary.getSpec().getRuntime();
        var template = runtime.getTemplates().getContainer();
        var storage = runtime.getStorage();
        var environment = runtime.getEnvironment();
        var jmx = runtime.getJmx();
        var metrics = runtime.getMetrics();
        var probePort = quarkus.getConfig().getProps().getOrDefault("http.port", 8080);
        var livenessProbe = template.getProbes().getLiveness();
        var readinessProbe = template.getProbes().getReadiness();
        var image = imageProvider.getImage(primary);

        var container = new ContainerBuilder()
                .withName("server")
                .withImage(image)
                .withLivenessProbe(new ProbeBuilder()
                        .withFailureThreshold(livenessProbe.getFailureThreshold())
                        .withInitialDelaySeconds(livenessProbe.getInitialDelaySeconds())
                        .withTimeoutSeconds(livenessProbe.getTimeoutSeconds())
                        .withPeriodSeconds(livenessProbe.getPeriodSeconds())
                        .withHttpGet(new HTTPGetActionBuilder()
                                .withPath("/q/health/live")
                                .withPort(new IntOrString(probePort))
                                .build())
                        .build())
                .withReadinessProbe(new ProbeBuilder()
                        .withFailureThreshold(readinessProbe.getFailureThreshold())
                        .withInitialDelaySeconds(readinessProbe.getInitialDelaySeconds())
                        .withTimeoutSeconds(readinessProbe.getTimeoutSeconds())
                        .withPeriodSeconds(readinessProbe.getPeriodSeconds())
                        .withHttpGet(new HTTPGetActionBuilder()
                                .withPath("/q/health/ready")
                                .withPort(new IntOrString(probePort))
                                .build())
                        .build())
                .withPorts(new ContainerPortBuilder()
                        .withName("http")
                        .withProtocol("TCP")
                        .withContainerPort(DEFAULT_HTTP_PORT)
                        .build())
                .addToVolumeMounts(new VolumeMountBuilder()
                        .withName(CONFIG_VOLUME_NAME)
                        .withMountPath(CONFIG_FILE_PATH)
                        .withSubPath(CONFIG_FILE_NAME)
                        .build())
                .build();

        addTemplateConfigurationToContainer(template, container);
        addEnvVariablesToContainer(environment, container);
        addStorageVolumeMountsToContainer(storage, container);
        addJmxConfigurationToContainer(jmx, container);
        addMetricConfigurationToContainer(metrics, container);
        return container;
    }

    private void addMetricConfigurationToContainer(Metrics metrics, Container container) {
        var jmxExporter = metrics.getJmxExporter();

        if (!jmxExporter.isEnabled()) {
            return;
        }

        // Add metrics port
        var portEnv = new EnvVar("JMX_EXPORTER_PORT", String.valueOf(DEFAULT_JMX_EXPORTER_METRICS_PORT), null);
        var port = new ContainerPortBuilder()
                .withName(JMX_EXPORTER_METRICS_PORT_NAME)
                .withProtocol("TCP")
                .withContainerPort(DEFAULT_JMX_EXPORTER_METRICS_PORT)
                .build();

        container.getEnv().add(portEnv);
        container.getPorts().add(port);

        // Mount metrics config file if required
        var configMapRef = metrics.getJmxExporter().getConfigFrom();
        if (configMapRef == null) {
            return;
        }

        var configFile = METRICS_FILE_PATH.formatted(configMapRef.getKey());
        var configEnv = new EnvVar("JMX_EXPORTER_CONFIG", configFile, null);
        var mount = new VolumeMountBuilder()
                .withName(METRICS_VOLUME_NAME)
                .withMountPath(METRICS_DIR_PATH)
                .build();

        container.getEnv().add(configEnv);
        container.getVolumeMounts().add(mount);
    }

    /**
     * Creates desired JMX init container
     *
     * @param jmx jmx configuration
     * @return init container or empty optional
     */
    private Optional<Container> desiredJmxInitContainer(JmxConfig jmx, String image) {
        var auth = jmx.getAuthentication();

        if (!auth.isEnabled()) {
            return Optional.empty();
        }

        var initContainer = new ContainerBuilder()
                .withName("server-init")
                .withImage(image)
                .withCommand("sh", "-c", JmxCmd.of(auth.getAccessFile()) + " && " + JmxCmd.of(auth.getPasswordFile()))
                .addToVolumeMounts(new VolumeMountBuilder()
                        .withName(JMX_CONFIG_VOLUME_INIT_NAME)
                        .withMountPath(JMX_CONFIG_VOLUME_INIT_PATH)
                        .build())
                .addToVolumeMounts(new VolumeMountBuilder()
                        .withName(JMX_CONFIG_VOLUME_NAME)
                        .withMountPath(JMX_CONFIG_VOLUME_PATH)
                        .build())
                .build();

        return Optional.of(initContainer);
    }

    /**
     * Adds storage volume mounts to container if required
     *
     * @param storage   runtime configuration
     * @param container target container
     */
    private void addStorageVolumeMountsToContainer(RuntimeStorage storage, Container container) {
        var volumeMounts = container.getVolumeMounts();

        // data volume
        desiredDataVolumeMount()
                .ifPresent(volumeMounts::add);

        // external volumes
        storage
                .getExternal()
                .stream()
                .map(volume -> new VolumeMountBuilder()
                        .withName(volume.getName())
                        .withMountPath(EXTERNAL_VOLUME_PATH.formatted(volume.getName()))
                        .build())
                .forEach(volumeMounts::add);
    }

    /**
     * Adds external environment variables to container in required
     *
     * @param environment environment configuration
     * @param container   target container
     */
    private void addEnvVariablesToContainer(RuntimeEnvironment environment, Container container) {
        var variables = environment.getVars()
                .stream()
                .map(ce -> new EnvVar(ce.getName(), ce.getValue(), null))
                .toList();

        container.getEnv().addAll(variables);
        container.getEnvFrom().addAll(environment.getFrom());
    }

    /**
     * Adds JMX configuration to container if required
     *
     * @param jmx       jmx configuration
     * @param container target container
     */
    private void addJmxConfigurationToContainer(JmxConfig jmx, Container container) {
        if (!jmx.isEnabled()) {
            return;
        }

        var ports = container.getPorts();
        ports.add(new ContainerPortBuilder()
                .withName("jmx")
                .withProtocol("TCP")
                .withContainerPort(jmx.getPort())
                .build());

        var opts = new HashMap<>(Map.of(
                "-Dcom.sun.management.jmxremote.ssl", false,
                "-Dcom.sun.management.jmxremote.port", jmx.getPort(),
                "-Dcom.sun.management.jmxremote.rmi.port", jmx.getPort(),
                "-Dcom.sun.management.jmxremote.local.only", false,
                "-Djava.rmi.server.hostname", "0.0.0.0",
                "-Dcom.sun.management.jmxremote.verbose", true,
                "-Dcom.sun.management.jmxremote.authenticate", false));

        var auth = jmx.getAuthentication();

        // If JMX authentication is enabled
        // Add JVM options and mount config files
        if (auth.isEnabled()) {
            opts.putAll(Map.of(
                    "-Dcom.sun.management.jmxremote.authenticate", true,
                    "-Dcom.sun.management.jmxremote.access.file", JMX_CONFIG_VOLUME_PATH + "/" + auth.getAccessFile(),
                    "-Dcom.sun.management.jmxremote.password.file", JMX_CONFIG_VOLUME_PATH + "/" + auth.getPasswordFile()));

            var mount = new VolumeMountBuilder()
                    .withName(JMX_CONFIG_VOLUME_NAME)
                    .withMountPath(JMX_CONFIG_VOLUME_PATH)
                    .withReadOnly(true)
                    .build();

            container.getVolumeMounts().add(mount);
        }

        // If JAVA_OPTS is already set (e.g. from container template) we don't want to override it
        mergeJavaOptsEnvVar(opts, container);
    }

    /**
     * Adds JAVA_OPTS environment variable if not set on container.
     * If JAVA_OPTS already exists then new (and only new) options are added
     *
     * @param newValue  additional JAVA_OPTS in form of a map
     * @param container target container
     */
    private void mergeJavaOptsEnvVar(Map<String, ?> newValue, Container container) {
        final var name = "JAVA_OPTS";
        var env = container.getEnv();

        var currentEnvVar = env.stream()
                .filter(envVar -> name.equals(envVar.getName()))
                .findFirst();
        // Remove current EnvVar instance if it exists
        currentEnvVar.ifPresent(env::remove);

        // Get current value as map
        var currentProperties = currentEnvVar
                .map(EnvVar::getValue)
                .map(StringUtils::splitJavaOpts)
                .orElse(Map.of());

        // Only set properties which are not set yet
        var additionalProperties = newValue.keySet()
                .stream()
                .filter(Predicate.not(currentProperties::containsKey))
                .collect(Collectors.toMap(k -> k, newValue::get));

        var mergedProperties = new HashMap<String, Object>();
        mergedProperties.putAll(currentProperties);
        mergedProperties.putAll(additionalProperties);

        env.add(new EnvVar(name, StringUtils.joinAsJavaOpts(mergedProperties), null));
    }

    /**
     * JMX auth file copy and permission command representation
     */
    private static final class JmxCmd {

        private final String source;
        private final String target;

        private JmxCmd(String file) {
            source = JMX_CONFIG_VOLUME_INIT_PATH + "/" + file;
            target = JMX_CONFIG_VOLUME_PATH + "/" + file;
        }

        @Override
        public String toString() {
            return "cp '%s' '%s' && chmod 600 '%s'".formatted(source, target, target);
        }

        public static JmxCmd of(String file) {
            return new JmxCmd(file);
        }
    }
}
