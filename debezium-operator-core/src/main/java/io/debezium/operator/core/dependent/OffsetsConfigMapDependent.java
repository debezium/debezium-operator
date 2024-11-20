/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core.dependent;

import static io.debezium.operator.api.model.source.storage.ConfigMapStore.MANAGED_CONFIG_MAP_NAME_TEMPLATE;

import io.debezium.operator.api.model.CommonLabels;
import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.core.dependent.discriminators.OffsetsConfigMapDiscriminator;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;

@KubernetesDependent(resourceDiscriminator = OffsetsConfigMapDiscriminator.class)
public class OffsetsConfigMapDependent extends CRUDKubernetesDependentResource<ConfigMap, DebeziumServer> {

    public static final String OFFSETS_CONFIG_MAP_CLASSIFIER = "offsets";

    public OffsetsConfigMapDependent() {
        super(ConfigMap.class);
    }

    @Override
    protected ConfigMap desired(DebeziumServer primary, Context<DebeziumServer> context) {

        var name = primary.getMetadata().getName();
        var labels = CommonLabels.serverComponent(name)
                .withDbzClassifier(OFFSETS_CONFIG_MAP_CLASSIFIER)
                .getMap();

        return new ConfigMapBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withNamespace(primary.getMetadata().getNamespace())
                        .withName(managedName(primary))
                        .withLabels(labels)
                        .build())
                .build();
    }

    public static String managedName(DebeziumServer primary) {
        return MANAGED_CONFIG_MAP_NAME_TEMPLATE.formatted(primary.getMetadata().getName());
    }
}
