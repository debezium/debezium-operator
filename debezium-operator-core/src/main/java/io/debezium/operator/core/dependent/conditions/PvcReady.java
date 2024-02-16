/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core.dependent.conditions;

import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.api.model.runtime.storage.StorageType;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResource;
import io.javaoperatorsdk.operator.processing.dependent.workflow.Condition;

public class PvcReady implements Condition<Deployment, DebeziumServer> {
    @Override
    public boolean isMet(
                         DependentResource<Deployment, DebeziumServer> dependentResource,
                         DebeziumServer primary,
                         Context<DebeziumServer> context) {
        var runtime = primary.getSpec().getRuntime();
        var dataStorage = runtime.getStorage().getData();

        if (dataStorage.getType() == StorageType.EPHEMERAL) {
            return true;
        }
        else if (dataStorage.getClaimName() != null) {
            return true;
        }

        return context
                .getSecondaryResource(PersistentVolumeClaim.class)
                .isPresent();
    }
}
