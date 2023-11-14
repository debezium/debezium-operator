/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.core.dependent.DeploymentDependent;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.ReconcilerUtils;
import io.quarkus.kubernetes.client.runtime.KubernetesClientUtils;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class DebeziumServerReconcilerTest {
    public static final String dsName = "test-ds";

    KubernetesClient client = KubernetesClientUtils.createClient();
    DebeziumServer debeziumServer;

    @BeforeEach
    void before() {
        debeziumServer = ReconcilerUtils.loadYaml(
                DebeziumServer.class, DebeziumServerReconcilerTest.class, "/test-samples/ds-postgres-minimal.yml");
        final var metadata = new ObjectMetaBuilder()
                .withName(dsName)
                .withNamespace(client.getNamespace())
                .build();
        debeziumServer.setMetadata(metadata);
    }

    @AfterEach
    void after() {
        client.resource(debeziumServer).delete();
    }

    @Test
    void shouldReconcileDebeziumServer() {
        client.resource(debeziumServer).create();
        await().ignoreException(NullPointerException.class).atMost(2, TimeUnit.MINUTES).untilAsserted(() -> {
            // check config map
            final var configMap = client.configMaps()
                    .inNamespace(debeziumServer.getMetadata().getNamespace())
                    .withName(debeziumServer.getMetadata().getName())
                    .get();
            assertThat(configMap).isNotNull();
            final var config = debeziumServer.asConfiguration();
            assertThat(configMap.getData()).containsEntry("application.properties", config.getAsString());

            // check deployment
            final var deployment = client.apps().deployments()
                    .inNamespace(debeziumServer.getMetadata().getNamespace())
                    .withName(debeziumServer.getMetadata().getName())
                    .get();
            assertThat(deployment).isNotNull();
            assertThat(deployment.getMetadata().getName()).isEqualTo(debeziumServer.getMetadata().getName());
            assertThat(deployment.getMetadata().getOwnerReferences()).anyMatch(owner -> owner.getName().equals(debeziumServer.getMetadata().getName()) &&
                    owner.getKind().equals(debeziumServer.getKind()));

            // check container
            final var maybeContainer = deployment.getSpec().getTemplate().getSpec().getContainers()
                    .stream()
                    .findFirst();
            assertThat(maybeContainer).isPresent();

            final var container = maybeContainer.get();
            assertThat(container.getImage()).isEqualTo(debeziumServer.getSpec().getImage());
            assertThat(container.getLivenessProbe()).isNotNull();
            assertThat(container.getReadinessProbe()).isNotNull();
            assertThat(container.getVolumeMounts()).hasSize(2);
            assertThat(container.getVolumeMounts()).anyMatch(mount -> mount.getName().equals(DeploymentDependent.CONFIG_VOLUME_NAME) &&
                    mount.getMountPath().equals(DeploymentDependent.CONFIG_FILE_PATH) &&
                    mount.getSubPath().equals(DeploymentDependent.CONFIG_FILE_NAME));
            assertThat(container.getVolumeMounts()).anyMatch(mount -> mount.getName().equals(DeploymentDependent.DATA_VOLUME_NAME) &&
                    mount.getMountPath().equals(DeploymentDependent.DATA_VOLUME_PATH));
        });
    }
}
