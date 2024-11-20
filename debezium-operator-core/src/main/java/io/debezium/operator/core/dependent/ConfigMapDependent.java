/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core.dependent;

import java.util.Map;

import io.debezium.operator.api.model.CommonLabels;
import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.core.dependent.discriminators.ServerConfigMapDiscriminator;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;

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

        return new ConfigMapBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withNamespace(primary.getMetadata().getNamespace())
                        .withName(primary.getMetadata().getName())
                        .withLabels(labels)
                        .build())
                .withData(Map.of(DeploymentDependent.CONFIG_FILE_NAME, config.getAsString()))
                .build();
    }
}
