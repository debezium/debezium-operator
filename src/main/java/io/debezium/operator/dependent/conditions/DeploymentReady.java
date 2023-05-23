/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.dependent.conditions;

import java.util.Objects;

import io.debezium.operator.DebeziumServer;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResource;
import io.javaoperatorsdk.operator.processing.dependent.workflow.Condition;

public class DeploymentReady implements Condition<Deployment, DebeziumServer> {
    @Override
    public boolean isMet(
                         DependentResource<Deployment, DebeziumServer> dependentResource,
                         DebeziumServer primary,
                         Context<DebeziumServer> context) {
        return dependentResource.getSecondaryResource(primary, context)
                .map(deployment -> Objects.equals(
                        deployment.getSpec().getReplicas(),
                        deployment.getStatus().getReadyReplicas()))
                .orElse(false);
    }
}
