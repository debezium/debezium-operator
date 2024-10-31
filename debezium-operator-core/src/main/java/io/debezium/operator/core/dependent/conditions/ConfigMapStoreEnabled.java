/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core.dependent.conditions;

import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.api.model.source.storage.offset.ConfigMapOffsetStore;
import io.fabric8.kubernetes.api.model.Service;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResource;
import io.javaoperatorsdk.operator.processing.dependent.workflow.Condition;

public class ConfigMapStoreEnabled implements Condition<Service, DebeziumServer> {

    @Override
    public boolean isMet(
                         DependentResource<Service, DebeziumServer> dependentResource,
                         DebeziumServer primary,
                         Context<DebeziumServer> context) {

        return primary.getSpec().getSource().getOffset().getActiveStore() instanceof ConfigMapOffsetStore;
    }
}
