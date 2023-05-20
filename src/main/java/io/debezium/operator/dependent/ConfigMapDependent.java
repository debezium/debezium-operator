/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.dependent;

import java.util.Map;

import io.debezium.operator.DebeziumServer;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;

public class ConfigMapDependent extends CRUDKubernetesDependentResource<ConfigMap, DebeziumServer> {

    public ConfigMapDependent() {
        super(ConfigMap.class);
    }

    @Override
    protected ConfigMap desired(DebeziumServer primary, Context<DebeziumServer> context) {
        var config = primary.asConfiguration();

        return new ConfigMapBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withNamespace(primary.getMetadata().getNamespace())
                        .withName(primary.getMetadata().getName())
                        .build())
                .withData(Map.of(DeploymentDependent.CONFIG_FILE_NAME, config.getAsString()))
                .build();
    }
}
