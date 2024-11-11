/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core.dependent;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.debezium.operator.api.config.ConfigMapping;
import io.debezium.operator.api.model.CommonLabels;
import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.core.dependent.discriminators.ServerConfigMapDiscriminator;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.javaoperatorsdk.operator.api.config.dependent.DependentResourceSpec;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import io.quarkus.logging.Log;

@KubernetesDependent(resourceDiscriminator = ServerConfigMapDiscriminator.class)
public class ConfigMapDependent extends CRUDKubernetesDependentResource<ConfigMap, DebeziumServer> {

    public static final String SERVER_CONFIG_CONFIG_MAP_CLASSIFIER = "config";

    public ConfigMapDependent() {
        super(ConfigMap.class);
    }

    @Override
    protected ConfigMap desired(DebeziumServer primary, Context<DebeziumServer> context) {
        var config = primary.asConfiguration();
        var name = primary.getMetadata().getName();
        var labels = CommonLabels.serverComponent(name)
                .withDbzClassifier(SERVER_CONFIG_CONFIG_MAP_CLASSIFIER)
                .getMap();

        var autoNamedDependents = context.getControllerConfiguration().getDependentResources().stream()
                .filter(isAutoNamed())
                .filter(isEnabled(primary, context))
                .map(DependentResourceSpec::getDependentResourceClass)
                .collect(Collectors.toSet());

        autoNamedDependents.forEach(dependentResource -> addAutoNamedConfig(primary, dependentResource, config));

        return new ConfigMapBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withNamespace(primary.getMetadata().getNamespace())
                        .withName(primary.getMetadata().getName())
                        .withLabels(labels)
                        .build())
                .withData(Map.of(DeploymentDependent.CONFIG_FILE_NAME, config.getAsString()))
                .build();
    }

    private static Predicate<DependentResourceSpec> isEnabled(DebeziumServer primary, Context<DebeziumServer> context) {
        return resourceSpec -> resourceSpec.getReconcileCondition().isMet(null, primary, context);
    }

    private static Predicate<DependentResourceSpec> isAutoNamed() {
        return resourceSpec -> AutoNamed.class.isAssignableFrom(resourceSpec.getDependentResourceClass());
    }

    private static void addAutoNamedConfig(DebeziumServer primary, Class<? extends DependentResource<?, ?>> dependentResource,
                                           ConfigMapping config) {

        try {

            DependentResource<?, ?> dependentResourceInstance = dependentResource.getDeclaredConstructor().newInstance();
            String configurationName = ((AutoNamed) dependentResourceInstance).configurationName();
            String configurationValue = ((AutoNamed) dependentResourceInstance).managedName(primary);
            config.put(configurationName, configurationValue);

        }
        catch (Exception e) {
            Log.error(String.format("Error while adding auto named configuration for %s", dependentResource), e);
            throw new RuntimeException(e);
        }
    }
}
