/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core.dependent.conditions;

import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.api.model.runtime.storage.StorageType;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResource;
import io.javaoperatorsdk.operator.processing.dependent.workflow.Condition;

public class CreatePvc implements Condition<PersistentVolumeClaim, DebeziumServer> {
    @Override
    public boolean isMet(
                         DependentResource<PersistentVolumeClaim, DebeziumServer> dependentResource,
                         DebeziumServer primary,
                         Context<DebeziumServer> context) {
        var runtime = primary.getSpec().getRuntime();
        var dataStorage = runtime.getStorage().getData();

        return dataStorage.getType() == StorageType.PERSISTENT &&
                dataStorage.getClaimName() == null;
    }
}
