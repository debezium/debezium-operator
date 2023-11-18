/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.debezium.operator.dependent;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.debezium.operator.DebeziumServer;
import io.debezium.operator.DebeziumServerReconciler;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.javaoperatorsdk.operator.ReconcilerUtils;
import io.javaoperatorsdk.operator.api.reconciler.Context;

public class ServiceAccountDependentTest {

    private final String MANAGED_SA_NAME = "test-ds-sa";
    private final String NAMESPACE = "debezium";
    private Context<DebeziumServer> context;
    private DebeziumServer debeziumServer;

    @BeforeEach
    void before() {
        debeziumServer = ReconcilerUtils.loadYaml(
                DebeziumServer.class, DebeziumServerReconciler.class, "ds-postgres-minimal.yml");
        final var metadata = new ObjectMetaBuilder()
                .withName("test-ds")
                .withNamespace(NAMESPACE)
                .build();
        debeziumServer.setMetadata(metadata);
    }

    @Test
    void shouldReturnServiceAccount() {
        String saName = "test-ds-service-account";
        debeziumServer.getSpec().getRuntime().setServiceAccount(saName);
        String sa = ServiceAccountDependent.serviceAccountNameFor(debeziumServer);

        assertThat(sa).isEqualTo(saName);
    }

    @Test
    void shouldReturnManagedServiceAccount() {
        String sa = ServiceAccountDependent.serviceAccountNameFor(debeziumServer);

        assertThat(sa).isEqualTo(MANAGED_SA_NAME);
    }

    @Test
    void shouldReturnDesiredServiceAccount() {
        ServiceAccountDependent serviceAccountDependent = new ServiceAccountDependent();
        ServiceAccount serviceAccount = serviceAccountDependent.desired(debeziumServer, context);

        assertThat(serviceAccount.getMetadata().getName()).isEqualTo(MANAGED_SA_NAME);
        assertThat(serviceAccount.getMetadata().getNamespace()).isEqualTo(NAMESPACE);
    }
}
