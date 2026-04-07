/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core.dependent;

import io.debezium.operator.api.model.CommonLabels;
import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.api.model.source.storage.ConfigMapStore;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import io.javaoperatorsdk.operator.processing.event.ResourceID;

@KubernetesDependent
public class OffsetsConfigMapDependent extends CRUDKubernetesDependentResource<ConfigMap, DebeziumServer> {

    public static final String OFFSETS_CONFIG_MAP_CLASSIFIER = "offsets";

    public OffsetsConfigMapDependent() {
        super(ConfigMap.class);
    }

    @Override
    protected ResourceID targetSecondaryResourceID(DebeziumServer primary, Context<DebeziumServer> context) {
        var name = primary.getMetadata().getName();
        var offsetConfig = primary.getSpec().getSource().getOffset();
        var configMapStore = offsetConfig.getConfigMap();

        // Use default name pattern from ConfigMapStore.MANAGED_CONFIG_MAP_NAME_TEMPLATE
        // to provide stable resource ID for JOSDK tracking, even when configMapStore is null.
        // The reconcilePrecondition controls whether this resource is actually created.
        String configMapName = (configMapStore != null)
                ? configMapStore.getFinalName(primary)
                : ConfigMapStore.MANAGED_CONFIG_MAP_NAME_TEMPLATE.formatted(name);

        return new ResourceID(configMapName, primary.getMetadata().getNamespace());
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
                        .withName(primary.getSpec().getSource().getOffset().getConfigMap().getFinalName(primary))
                        .withLabels(labels)
                        .build())
                .build();
    }

}
