/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core.dependent.conditions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.debezium.doc.FixFor;
import io.debezium.operator.api.model.DebeziumServer;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResource;
import io.javaoperatorsdk.operator.api.reconciler.dependent.ReconcileResult;

class DeploymentReadyTest {

    private final DeploymentReady condition = new DeploymentReady();

    @Test
    @FixFor("debezium/dbz#2528")
    void shouldBeMetWhenStoppedAndReadyReplicasOmitted() {
        // A stopped server is scaled to 0 replicas; Kubernetes then omits readyReplicas
        // from the status, which must be interpreted as 0.
        var deployment = new DeploymentBuilder()
                .withNewSpec().withReplicas(0).endSpec()
                .withNewStatus().endStatus()
                .build();
        assertThat(condition.isMet(dependentReturning(deployment), null, null)).isTrue();
    }

    @Test
    @FixFor("debezium/dbz#2528")
    void shouldBeMetWhenRunningAndReadyReplicasMatch() {
        var deployment = new DeploymentBuilder()
                .withNewSpec().withReplicas(1).endSpec()
                .withNewStatus().withReadyReplicas(1).endStatus()
                .build();
        assertThat(condition.isMet(dependentReturning(deployment), null, null)).isTrue();
    }

    @Test
    @FixFor("debezium/dbz#2528")
    void shouldNotBeMetWhenRunningButReadyReplicasOmitted() {
        var deployment = new DeploymentBuilder()
                .withNewSpec().withReplicas(1).endSpec()
                .withNewStatus().endStatus()
                .build();
        assertThat(condition.isMet(dependentReturning(deployment), null, null)).isFalse();
    }

    @Test
    @FixFor("debezium/dbz#2528")
    void shouldNotBeMetWhenSecondaryResourceMissing() {
        assertThat(condition.isMet(dependentReturning(null), null, null)).isFalse();
    }

    private static DependentResource<Deployment, DebeziumServer> dependentReturning(Deployment deployment) {
        return new DependentResource<>() {
            @Override
            public Optional<Deployment> getSecondaryResource(DebeziumServer primary, Context<DebeziumServer> context) {
                return Optional.ofNullable(deployment);
            }

            @Override
            public ReconcileResult<Deployment> reconcile(DebeziumServer primary, Context<DebeziumServer> context) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Class<Deployment> resourceType() {
                return Deployment.class;
            }
        };
    }
}
