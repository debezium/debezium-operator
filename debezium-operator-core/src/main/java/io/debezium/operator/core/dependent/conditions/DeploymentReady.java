/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core.dependent.conditions;

import io.debezium.operator.api.model.DebeziumServer;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentStatus;
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
                .map(DeploymentReady::isReady)
                .orElse(false);
    }

    private static boolean isReady(Deployment deployment) {
        // The deployment is ready when the ready replicas match the desired count. A stopped
        // server is scaled to 0 replicas; Kubernetes then omits readyReplicas from the status,
        // so it must be read as 0 (see readyReplicas) for 0 == 0 to hold (dbz#2528).
        return desiredReplicas(deployment) == readyReplicas(deployment);
    }

    private static int desiredReplicas(Deployment deployment) {
        var replicas = deployment.getSpec().getReplicas();
        return replicas == null ? 0 : replicas;
    }

    private static int readyReplicas(Deployment deployment) {
        // Kubernetes omits readyReplicas from the status when it is 0 (e.g. a stopped
        // server scaled to 0 replicas), so a null value must be treated as 0.
        DeploymentStatus status = deployment.getStatus();
        if (status == null || status.getReadyReplicas() == null) {
            return 0;
        }
        return status.getReadyReplicas();
    }
}
