/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.dependent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.debezium.operator.DebeziumServer;
import io.debezium.operator.VersionProvider;
import io.debezium.operator.model.CommonLabels;
import io.debezium.operator.model.JmxConfig;
import io.debezium.operator.model.Runtime;
import io.debezium.operator.model.templates.ContainerTemplate;
import io.debezium.operator.model.templates.PodTemplate;
import io.debezium.operator.util.StringUtils;
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
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
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
    public static final String CONFIG_FILE_PATH = "/debezium/conf/" + CONFIG_FILE_NAME;
    public static final String DATA_VOLUME_NAME = "ds-data";
    public static final String DATA_VOLUME_PATH = "/debezium/data";
    public static final String EXTERNAL_VOLUME_PATH = "/debezium/external-configuration/%s";
    public static final int DEFAULT_HTTP_PORT = 8080;
    private static final String CONFIG_MD5_ANNOTATION = "debezium.io/server-config-md5";

    @ConfigProperty(name = "debezium.image", defaultValue = DEFAULT_IMAGE)
    String defaultImage;

    @Inject
    VersionProvider version;

    public DeploymentDependent() {
        super(Deployment.class);
    }

    @Override
    protected Deployment desired(DebeziumServer primary, Context<DebeziumServer> context) {
        var runtime = primary.getSpec().getRuntime();
        var templates = runtime.getTemplates();
        var name = primary.getMetadata().getName();
        var labels = CommonLabels.serverComponent(name).getMap();
        var annotations = Map.of(CONFIG_MD5_ANNOTATION, primary.asConfiguration().md5Sum());

        var sa = context.getSecondaryResource(ServiceAccount.class)
                .map(r -> r.getMetadata().getName())
                .orElseThrow();

        var desiredContainer = desiredServerContainer(primary);
        var dataVolume = desiredDataVolume(primary);

        var pod = new PodTemplateSpecBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withLabels(labels)
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
                        .addToVolumes(dataVolume)
                        .addToContainers(desiredContainer)
                        .build())
                .build();

        addTemplateConfigurationToPod(templates.getPod(), pod);
        addExternalVolumesToPod(runtime, pod);

        return new DeploymentBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withNamespace(primary.getMetadata().getNamespace())
                        .withName(name)
                        .withLabels(labels)
                        .withAnnotations(annotations)
                        .build())
                .withSpec(new DeploymentSpecBuilder()
                        .withSelector(new LabelSelectorBuilder()
                                .addToMatchLabels(labels)
                                .build())
                        .withTemplate(pod)
                        .build())
                .build();
    }

    /**
     * Applies pod template configuration to pod if required
     *
     * @param template pod template configuration
     * @param pod actual pod template spec
     */
    private void addTemplateConfigurationToPod(PodTemplate template, PodTemplateSpec pod) {
        var templateMeta = template.getMetadata();
        var podSpec = pod.getSpec();
        var podMeta = pod.getMetadata();

        podSpec.setAffinity(template.getAffinity());
        podSpec.setImagePullSecrets(template.getImagePullSecrets());
        podMeta.getLabels().putAll(templateMeta.getLabels());
        podMeta.getAnnotations().putAll(templateMeta.getAnnotations());
    }

    /**
     * Adds external volume definitions to pod if required
     *
     * @param runtime runtime configuration
     * @param pod actual pod template spec
     */
    private void addExternalVolumesToPod(Runtime runtime, PodTemplateSpec pod) {
        var volumes = pod.getSpec().getVolumes();
        volumes.addAll(runtime.getVolumes());
    }

    private void addTemplateConfigurationToContainer(ContainerTemplate template, Container container) {
        var containerEnv = template.getEnv()
                .stream()
                .map(ce -> new EnvVar(ce.getName(), ce.getValue(), null))
                .toList();

        container.getEnv().addAll(containerEnv);
        container.setSecurityContext(template.getSecurityContext());
        container.setResources(template.getResources());
    }

    /**
     * Creates desired data volume
     *
     * @param primary primary CR
     * @return desired data volume
     */
    private Volume desiredDataVolume(DebeziumServer primary) {
        var storageConfig = primary.getSpec().getStorage();
        var builder = new VolumeBuilder().withName(DATA_VOLUME_NAME);

        switch (storageConfig.getType()) {
            case EPHEMERAL -> builder.withEmptyDir(new EmptyDirVolumeSourceBuilder()
                    .build());
            case PERSISTENT -> builder.withPersistentVolumeClaim(new PersistentVolumeClaimVolumeSourceBuilder()
                    .withClaimName(storageConfig.getClaimName())
                    .build());
        }
        return builder.build();
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
        var jmx = primary.getSpec().getRuntime().getJmx();
        var probePort = quarkus.getConfig().getProps().getOrDefault("http.port", 8080);
        var image = getTaggedImage(primary);

        var container = new ContainerBuilder()
                .withName("server")
                .withImage(image)
                .withLivenessProbe(new ProbeBuilder()
                        .withHttpGet(new HTTPGetActionBuilder()
                                .withPath("/q/health/live")
                                .withPort(new IntOrString(probePort))
                                .build())
                        .build())
                .withReadinessProbe(new ProbeBuilder()
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
                .addToVolumeMounts(new VolumeMountBuilder()
                        .withName(DATA_VOLUME_NAME)
                        .withMountPath(DATA_VOLUME_PATH)
                        .build())
                .build();

        addTemplateConfigurationToContainer(template, container);
        addExternalEnvVariablesToContainer(runtime, container);
        addExternalVolumeMountsToContainer(runtime, container);
        addJmxConfigurationToContainer(jmx, container);
        return container;
    }

    /**
     * Adds external volume mounts to container if required
     *
     * @param runtime runtime configuration
     * @param container target container
     */
    private void addExternalVolumeMountsToContainer(Runtime runtime, Container container) {
        var volumeMounts = runtime.getVolumes().stream()
                .map(volume -> new VolumeMountBuilder()
                        .withName(volume.getName())
                        .withMountPath(EXTERNAL_VOLUME_PATH.formatted(volume.getName()))
                        .withReadOnly()
                        .build())
                .toList();

        container.getVolumeMounts().addAll(volumeMounts);
    }

    /**
     * Adds external environment variables to container in required
     *
     * @param runtime runtime configuration
     * @param container target container
     */
    private void addExternalEnvVariablesToContainer(Runtime runtime, Container container) {
        container.getEnvFrom().addAll(runtime.getEnv());
    }

    /**
     * Adds JMX configuration to container if required
     *
     * @param jmx jmx configuration
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

        var opts = Map.of(
                "-Dcom.sun.management.jmxremote.ssl", false,
                "-Dcom.sun.management.jmxremote.port", jmx.getPort(),
                "-Dcom.sun.management.jmxremote.rmi.port", jmx.getPort(),
                "-Dcom.sun.management.jmxremote.local.only", false,
                "-Djava.rmi.server.hostname", "0.0.0.0",
                "-Dcom.sun.management.jmxremote.verbose", true,
                "-Dcom.sun.management.jmxremote.authenticate", false);

        // If JAVA_OPTS is already set (e.g. from container template) we don't want to override it
        mergeJavaOptsEnvVar(opts, container);
    }

    /**
     * Adds JAVA_OPTS environment variable is not set on container.
     * If JAVA_OPTS already exists then new (and only new) options are added
     *
     * @param newValue additional JAVA_OPTS in form of a map
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
     * Determines the debezium server image tag
     *
     * @param primary primary CR
     * @return image tag
     */
    private String getTaggedImage(DebeziumServer primary) {
        var image = primary.getSpec().getImage();

        if (image == null) {
            image = defaultImage + ":" + version.getImageTag(primary);
        }

        return image;
    }
}
