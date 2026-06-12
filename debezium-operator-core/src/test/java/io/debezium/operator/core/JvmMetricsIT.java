/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricExporter;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(JvmMetricsIT.OtelMetricsProfile.class)
public class JvmMetricsIT {

    public static class OtelMetricsProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "quarkus.otel.metrics.enabled", "true",
                    "quarkus.otel.instrument.jvm-metrics", "true",
                    "quarkus.otel.metric.export.interval", "500ms");
        }
    }

    @jakarta.enterprise.inject.Produces
    @jakarta.inject.Singleton
    public static InMemoryMetricExporter inMemoryMetricExporter() {
        return InMemoryMetricExporter.create();
    }

    @Inject
    InMemoryMetricExporter metricExporter;

    @Test
    public void shouldExposeJvmMetrics() {
        await().atMost(60, java.util.concurrent.TimeUnit.SECONDS).untilAsserted(() -> {
            List<MetricData> metrics = metricExporter.getFinishedMetricItems();
            assertThat(metrics).isNotEmpty();

            boolean hasJvmMetrics = metrics.stream()
                    .anyMatch(m -> m.getName().startsWith("jvm.memory") || m.getName().startsWith("process.runtime.jvm"));

            assertThat(hasJvmMetrics)
                    .withFailMessage("Expected JVM metrics (starting with jvm.memory or process.runtime.jvm) to be collected but found none.")
                    .isTrue();
        });
    }
}
