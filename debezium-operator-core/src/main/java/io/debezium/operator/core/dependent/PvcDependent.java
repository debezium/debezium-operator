/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core.dependent;

import java.util.Objects;

import io.debezium.operator.api.model.DebeziumServer;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;

public class PvcDependent extends CRUDKubernetesDependentResource<PersistentVolumeClaim, DebeziumServer> {

    public static final String MANAGED_PVC_NAME = "%s-data-volume-claim";

    public PvcDependent() {
        super(PersistentVolumeClaim.class);
    }

    @Override
    protected PersistentVolumeClaim desired(DebeziumServer primary, Context<DebeziumServer> context) {
        var template = primary.getSpec().getRuntime().getTemplates().getVolumeClaim();

        if (template == null) {
            throw new IllegalStateException("Missing PVC template");
        }

        return new PersistentVolumeClaimBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName(MANAGED_PVC_NAME.formatted(primary.getMetadata().getName()))
                        .withNamespace(primary.getMetadata().getNamespace())
                        .build())
                .withSpec(template)
                .build();
    }

    private static String managedPvcNameFor(DebeziumServer primary) {
        var name = primary.getMetadata().getName();
        return MANAGED_PVC_NAME.formatted(name);
    }

    public static String pvcNameFor(DebeziumServer primary) {
        var runtime = primary.getSpec().getRuntime();
        var storage = runtime.getStorage().getData();
        var providedPvcName = storage.getClaimName();
        var managedPvcName = managedPvcNameFor(primary);

        return Objects.requireNonNullElse(providedPvcName, managedPvcName);
    }
}
