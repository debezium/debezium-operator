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
import io.debezium.operator.api.model.runtime.metrics.OtelCollectorBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.ReconcilerUtilsInternal;
import io.quarkus.kubernetes.client.runtime.internal.KubernetesClientUtils;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class OpenTelemetryReconcilerTest {

    public static final String DS_NAME = "test-ds-otel";

    KubernetesClient client = KubernetesClientUtils.createClient();
    DebeziumServer debeziumServer;

    @BeforeEach
    void before() {
        debeziumServer = ReconcilerUtilsInternal.loadYaml(
                DebeziumServer.class, OpenTelemetryReconcilerTest.class, "/test-samples/ds-postgres-minimal.yml");
        final var metadata = new ObjectMetaBuilder()
                .withName(DS_NAME)
                .withNamespace(client.getNamespace())
                .build();
        debeziumServer.setMetadata(metadata);
    }

    @AfterEach
    void after() {
        client.resource(debeziumServer).delete();
    }

    @Test
    void shouldNotSetOtelEnvVarsWhenDisabled() {
        client.resource(debeziumServer).create();
        await().ignoreException(NullPointerException.class).atMost(2, TimeUnit.MINUTES).untilAsserted(() -> {
            final var deployment = client.apps().deployments()
                    .inNamespace(debeziumServer.getMetadata().getNamespace())
                    .withName(DS_NAME)
                    .get();
            assertThat(deployment).isNotNull();

            final var container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
            assertThat(container.getEnv())
                    .extracting(EnvVar::getName)
                    .doesNotContain("OTEL_ENABLED", "OTEL_SDK_DISABLED", "OTEL_SERVICE_NAME");
        });
    }

    @Test
    void shouldSetOtelEnvVarsWhenEnabled() {
        debeziumServer.getSpec().getRuntime().getMetrics().getOpenTelemetry().setEnabled(true);

        client.resource(debeziumServer).create();
        await().ignoreException(NullPointerException.class).atMost(2, TimeUnit.MINUTES).untilAsserted(() -> {
            final var deployment = client.apps().deployments()
                    .inNamespace(debeziumServer.getMetadata().getNamespace())
                    .withName(DS_NAME)
                    .get();
            assertThat(deployment).isNotNull();

            final var container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
            var envVars = container.getEnv();

            assertThat(envVars).anyMatch(e -> "OTEL_ENABLED".equals(e.getName()) && "true".equals(e.getValue()));
            assertThat(envVars).anyMatch(e -> "OTEL_SDK_DISABLED".equals(e.getName()) && "false".equals(e.getValue()));
            assertThat(envVars).anyMatch(e -> "OTEL_METRICS_EXPORTER".equals(e.getName()) && "otlp".equals(e.getValue()));
            assertThat(envVars).anyMatch(e -> "OTEL_SERVICE_NAME".equals(e.getName()) && DS_NAME.equals(e.getValue()));
            assertThat(envVars).anyMatch(e -> "OTEL_EXPORTER_OTLP_ENDPOINT".equals(e.getName())
                    && "http://localhost:4318".equals(e.getValue()));
            assertThat(envVars).anyMatch(e -> "OTEL_JMX_CONFIG".equals(e.getName())
                    && "config/debezium-jmx-config.yaml".equals(e.getValue()));
            assertThat(envVars).anyMatch(e -> "OTEL_RESOURCE_ATTRIBUTES".equals(e.getName())
                    && e.getValue().contains("service.name=" + DS_NAME)
                    && e.getValue().contains("debezium.connector.type=postgresql"));
        });
    }

    @Test
    void shouldUseCustomEndpointWhenProvided() {
        var otel = debeziumServer.getSpec().getRuntime().getMetrics().getOpenTelemetry();
        otel.setEnabled(true);
        otel.setCollector(new OtelCollectorBuilder()
                .withEndpoint("http://my-collector:4317")
                .build());

        client.resource(debeziumServer).create();
        await().ignoreException(NullPointerException.class).atMost(2, TimeUnit.MINUTES).untilAsserted(() -> {
            final var deployment = client.apps().deployments()
                    .inNamespace(debeziumServer.getMetadata().getNamespace())
                    .withName(DS_NAME)
                    .get();
            assertThat(deployment).isNotNull();

            final var container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
            assertThat(container.getEnv())
                    .anyMatch(e -> "OTEL_EXPORTER_OTLP_ENDPOINT".equals(e.getName())
                            && "http://my-collector:4317".equals(e.getValue()));
        });
    }
}
