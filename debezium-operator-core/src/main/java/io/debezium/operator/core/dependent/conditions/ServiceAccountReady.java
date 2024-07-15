/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core.dependent.conditions;

import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.core.dependent.ServiceAccountDependent;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResource;
import io.javaoperatorsdk.operator.processing.dependent.workflow.Condition;

public class ServiceAccountReady implements Condition<ServiceAccount, DebeziumServer> {
    @Override
    public boolean isMet(
                         DependentResource<ServiceAccount, DebeziumServer> dependentResource,
                         DebeziumServer primary,
                         Context<DebeziumServer> context) {
        var namespace = primary.getMetadata().getNamespace();
        var serviceAccountName = ServiceAccountDependent.serviceAccountNameFor(primary);
        return context.getClient().serviceAccounts().inNamespace(namespace).withName(serviceAccountName).get() != null;
    }
}
