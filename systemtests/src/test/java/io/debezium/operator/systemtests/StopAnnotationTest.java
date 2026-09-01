/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.systemtests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.debezium.doc.FixFor;
import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.api.model.status.Condition;
import io.debezium.operator.systemtests.resources.NamespaceHolder;
import io.debezium.operator.systemtests.resources.operator.DebeziumOperatorBundleResource;
import io.debezium.operator.systemtests.resources.server.DebeziumServerGenerator;
import io.debezium.operator.systemtests.resources.server.DebeziumServerResource;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.skodjob.kubetest4j.resources.KubeResourceManager;

public class StopAnnotationTest extends TestBase {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    @FixFor("debezium/dbz#2528")
    void testStopAnnotationStopsAndResumesServer() {
        String namespace = NamespaceHolder.INSTANCE.getCurrentNamespace();
        DebeziumOperatorBundleResource operatorBundleResource = new DebeziumOperatorBundleResource();
        operatorBundleResource.configureAsDefault(namespace);
        logger.info("Deploying Operator");
        operatorBundleResource.deploy();

        DebeziumServer server = DebeziumServerGenerator.generateDefaultMysqlToRedis(namespace);
        String name = server.getMetadata().getName();

        logger.info("Deploying Debezium Server");
        KubeResourceManager.get().createResourceWithWait(server);
        assertStreamingWorks();
        assertThat(deploymentReplicas(namespace, name)).isEqualTo(1);

        // Stop the server by adding the debezium.io/stop annotation.
        logger.info("Stopping Debezium Server via debezium.io/stop annotation");
        if (server.getMetadata().getAnnotations() == null) {
            server.getMetadata().setAnnotations(new HashMap<>());
        }
        server.setStopped(true);
        KubeResourceManager.get().createOrUpdateResourceWithWait(server);

        // The Ready condition stays True while stopped (0 == 0 replicas ready), so we must poll
        // the Deployment replicas and the Running condition to observe that reconciliation
        // actually scaled the server down.
        await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(deploymentReplicas(namespace, name)).isEqualTo(0);
            Condition running = runningCondition(namespace, name);
            assertThat(running).isNotNull();
            assertThat(running.getStatus()).isEqualTo("False");
            assertThat(running.getMessage()).isEqualTo("Server %s is stopped".formatted(name));
        });

        // The stopped state must be stable. Once scaled to 0 replicas, Kubernetes omits
        // readyReplicas from the Deployment status; if that omitted value were not read as 0
        // (the dbz#2528 bug), the server would never be reported ready and would flip to
        // "deployment in progress". Assert the stopped, ready condition holds continuously so
        // that such a regression fails the test instead of passing on the first good poll.
        await().during(Duration.ofSeconds(45)).atMost(Duration.ofSeconds(75)).pollInterval(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(deploymentReplicas(namespace, name)).isEqualTo(0);
            Condition running = runningCondition(namespace, name);
            assertThat(running).isNotNull();
            assertThat(running.getStatus()).isEqualTo("False");
            assertThat(running.getMessage()).isEqualTo("Server %s is stopped".formatted(name));
            Condition ready = readyCondition(namespace, name);
            assertThat(ready).isNotNull();
            assertThat(ready.getStatus()).isEqualTo("True");
        });

        // Resume the server by removing the annotation.
        logger.info("Resuming Debezium Server by removing debezium.io/stop annotation");
        server.setStopped(false);
        KubeResourceManager.get().createOrUpdateResourceWithWait(server);

        await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(deploymentReplicas(namespace, name)).isEqualTo(1);
            Condition running = runningCondition(namespace, name);
            assertThat(running).isNotNull();
            assertThat(running.getStatus()).isEqualTo("True");
        });
        assertStreamingWorks();
    }

    private static int deploymentReplicas(String namespace, String name) {
        Deployment deployment = KubeResourceManager.get().kubeClient().getClient()
                .apps().deployments()
                .inNamespace(namespace)
                .withName(name)
                .get();
        if (deployment == null || deployment.getSpec().getReplicas() == null) {
            return 0;
        }
        return deployment.getSpec().getReplicas();
    }

    private static Condition runningCondition(String namespace, String name) {
        return conditionOfType(namespace, name, "Running");
    }

    private static Condition readyCondition(String namespace, String name) {
        return conditionOfType(namespace, name, "Ready");
    }

    private static Condition conditionOfType(String namespace, String name, String type) {
        DebeziumServer server = new DebeziumServerResource().get(namespace, name);
        if (server == null || server.getStatus() == null || server.getStatus().getConditions() == null) {
            return null;
        }
        return server.getStatus().getConditions().stream()
                .filter(condition -> condition.getType().equals(type))
                .findFirst()
                .orElse(null);
    }
}
