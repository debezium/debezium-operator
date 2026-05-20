/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.systemtests;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.api.model.runtime.metrics.OtelCollectorBuilder;
import io.debezium.operator.systemtests.resources.NamespaceHolder;
import io.debezium.operator.systemtests.resources.operator.DebeziumOperatorBundleResource;
import io.debezium.operator.systemtests.resources.server.DebeziumServerGenerator;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.skodjob.testframe.resources.KubeResourceManager;

public class OpenTelemetryTest extends TestBase {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    void testOpenTelemetryEnvVarsAppliedToDeployment() {
        String namespace = NamespaceHolder.INSTANCE.getCurrentNamespace();
        DebeziumOperatorBundleResource operatorBundleResource = new DebeziumOperatorBundleResource();
        operatorBundleResource.configureAsDefault(namespace);
        logger.info("Deploying Operator");
        operatorBundleResource.deploy();

        DebeziumServer server = DebeziumServerGenerator.generateDefaultMysqlToRedis(namespace);
        server.getSpec().getRuntime().getMetrics().getOpenTelemetry().setEnabled(true);

        logger.info("Deploying Debezium Server with OpenTelemetry enabled");
        KubeResourceManager.getInstance().createResourceWithWait(server);
        assertStreamingWorks();

        Deployment deployment = KubeResourceManager.getKubeClient().getClient()
                .apps().deployments()
                .inNamespace(namespace)
                .withName(server.getMetadata().getName())
                .get();

        assertThat(deployment).isNotNull();

        var container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
        var envVars = container.getEnv();

        assertThat(envVars).anyMatch(e -> "OTEL_ENABLED".equals(e.getName()) && "true".equals(e.getValue()));
        assertThat(envVars).anyMatch(e -> "OTEL_SDK_DISABLED".equals(e.getName()) && "false".equals(e.getValue()));
        assertThat(envVars).anyMatch(e -> "OTEL_METRICS_EXPORTER".equals(e.getName()) && "otlp".equals(e.getValue()));
        assertThat(envVars).anyMatch(e -> "OTEL_JMX_CONFIG".equals(e.getName()));
        assertThat(envVars).anyMatch(e -> "OTEL_SERVICE_NAME".equals(e.getName())
                && server.getMetadata().getName().equals(e.getValue()));
        assertThat(envVars).anyMatch(e -> "OTEL_EXPORTER_OTLP_ENDPOINT".equals(e.getName()));
        assertThat(envVars).anyMatch(e -> "OTEL_RESOURCE_ATTRIBUTES".equals(e.getName())
                && e.getValue().contains("service.name=" + server.getMetadata().getName())
                && e.getValue().contains("debezium.connector.type=mysql"));
    }

    @Test
    void testOpenTelemetryWithCustomEndpoint() {
        String namespace = NamespaceHolder.INSTANCE.getCurrentNamespace();
        DebeziumOperatorBundleResource operatorBundleResource = new DebeziumOperatorBundleResource();
        operatorBundleResource.configureAsDefault(namespace);
        logger.info("Deploying Operator");
        operatorBundleResource.deploy();

        String customEndpoint = "http://otel-collector:4317";
        DebeziumServer server = DebeziumServerGenerator.generateDefaultMysqlToRedis(namespace);
        var otel = server.getSpec().getRuntime().getMetrics().getOpenTelemetry();
        otel.setEnabled(true);
        otel.setCollector(new OtelCollectorBuilder()
                .withEndpoint(customEndpoint)
                .build());

        logger.info("Deploying Debezium Server with custom OpenTelemetry endpoint");
        KubeResourceManager.getInstance().createResourceWithWait(server);
        assertStreamingWorks();

        Deployment deployment = KubeResourceManager.getKubeClient().getClient()
                .apps().deployments()
                .inNamespace(namespace)
                .withName(server.getMetadata().getName())
                .get();

        assertThat(deployment).isNotNull();

        var container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
        assertThat(container.getEnv())
                .anyMatch(e -> "OTEL_EXPORTER_OTLP_ENDPOINT".equals(e.getName())
                        && customEndpoint.equals(e.getValue()));
    }
}
