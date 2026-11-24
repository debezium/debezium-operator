/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.systemtests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

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
import io.skodjob.kubetest4j.resources.KubeResourceManager;

/**
 * Verifies that terminal pod failures are reported in the {@link DebeziumServer} status
 * conditions, separately from the regular "deployment in progress" state.
 */
public class PodFailureDetectionTest extends TestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(PodFailureDetectionTest.class);
    private static final String INVALID_IMAGE = "invalid@@image";
    private static final String DEFAULT_IMAGE = "quay.io/debezium/server:nightly";

    @Test
    @FixFor("debezium/dbz#2857")
    void shouldReportPodFailureAndClearItAfterRecovery() {
        String namespace = NamespaceHolder.INSTANCE.getCurrentNamespace();
        DebeziumOperatorBundleResource operatorBundleResource = new DebeziumOperatorBundleResource();
        operatorBundleResource.configureAsDefault(namespace);
        LOGGER.info("Deploying Operator");
        operatorBundleResource.deploy();

        DebeziumServer server = DebeziumServerGenerator.generateDefaultMysqlToRedis(namespace);
        String name = server.getMetadata().getName();
        server.getSpec().setImage(INVALID_IMAGE);

        LOGGER.info("Deploying Debezium Server with invalid image");
        KubeResourceManager.get().createResourceWithoutWait(server);

        await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(5)).untilAsserted(() -> {
            Condition ready = conditionOfType(namespace, name, "Ready");
            assertThat(ready).isNotNull();
            assertThat(ready.getStatus()).isEqualTo(Condition.FALSE);
            assertThat(ready.getMessage()).isEqualTo("Server %s deployment in progress".formatted(name));

            Condition running = conditionOfType(namespace, name, "Running");
            assertThat(running).isNotNull();
            assertThat(running.getStatus()).isEqualTo(Condition.FALSE);
            assertThat(running.getMessage())
                    .startsWith("Server %s has failed: InvalidImageName".formatted(name));
        });

        LOGGER.info("Restoring a valid image and waiting for recovery");
        server.getSpec().setImage(DEFAULT_IMAGE);
        KubeResourceManager.get().createOrUpdateResourceWithWait(server);

        await().atMost(Duration.ofMinutes(3)).pollInterval(Duration.ofSeconds(5)).untilAsserted(() -> {
            Condition ready = conditionOfType(namespace, name, "Ready");
            assertThat(ready).isNotNull();
            assertThat(ready.getStatus()).isEqualTo(Condition.TRUE);

            Condition running = conditionOfType(namespace, name, "Running");
            assertThat(running).isNotNull();
            assertThat(running.getStatus()).isEqualTo(Condition.TRUE);
            assertThat(running.getMessage()).isEqualTo("Server %s is running".formatted(name));
        });
    }

    /**
     * Same scenario as the kind E2E runbook: a working pipeline whose startup is broken by a bad
     * configuration value, producing a real {@code CrashLoopBackOff}, followed by recovery.
     * <p>
     * Note: an invalid connector class does not crash Debezium Server 3.7, so an invalid value
     * for an eagerly parsed Quarkus property is used instead.
     */
    @Test
    @FixFor("debezium/dbz#2857")
    void shouldReportCrashLoopBackOffFromBadConfigurationAndRecover() {
        String namespace = NamespaceHolder.INSTANCE.getCurrentNamespace();
        DebeziumOperatorBundleResource operatorBundleResource = new DebeziumOperatorBundleResource();
        operatorBundleResource.configureAsDefault(namespace);
        LOGGER.info("Deploying Operator");
        operatorBundleResource.deploy();

        DebeziumServer server = DebeziumServerGenerator.generateDefaultMysqlToRedis(namespace);
        // Distinct name from the invalid-image test, which shares this namespace.
        server.getMetadata().setName("my-debezium-bad-config");
        String name = server.getMetadata().getName();

        LOGGER.info("Deploying Debezium Server with a working pipeline");
        KubeResourceManager.get().createResourceWithWait(server);
        assertStreamingWorks();

        LOGGER.info("Breaking startup configuration to trigger CrashLoopBackOff");
        server.getSpec().getQuarkus().getConfig().getProps().put("http.port", "abc");
        KubeResourceManager.get().createOrUpdateResourceWithoutWait(server);

        await().atMost(Duration.ofMinutes(3)).pollInterval(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(containerWaitingReason(namespace, name)).isEqualTo("CrashLoopBackOff");

            Condition ready = conditionOfType(namespace, name, "Ready");
            assertThat(ready).isNotNull();
            assertThat(ready.getStatus()).isEqualTo(Condition.FALSE);
            assertThat(ready.getMessage()).isEqualTo("Server %s deployment in progress".formatted(name));

            Condition running = conditionOfType(namespace, name, "Running");
            assertThat(running).isNotNull();
            assertThat(running.getStatus()).isEqualTo(Condition.FALSE);
            assertThat(running.getMessage())
                    .startsWith("Server %s has failed: CrashLoopBackOff".formatted(name));
        });

        LOGGER.info("Restoring configuration and waiting for recovery");
        server.getSpec().getQuarkus().getConfig().getProps().remove("http.port");
        KubeResourceManager.get().createOrUpdateResourceWithWait(server);

        await().atMost(Duration.ofMinutes(3)).pollInterval(Duration.ofSeconds(5)).untilAsserted(() -> {
            Condition ready = conditionOfType(namespace, name, "Ready");
            assertThat(ready).isNotNull();
            assertThat(ready.getStatus()).isEqualTo(Condition.TRUE);

            Condition running = conditionOfType(namespace, name, "Running");
            assertThat(running).isNotNull();
            assertThat(running.getStatus()).isEqualTo(Condition.TRUE);
            assertThat(running.getMessage()).isEqualTo("Server %s is running".formatted(name));
        });

        assertStreamingWorks();
    }

    private static String containerWaitingReason(String namespace, String name) {
        return KubeResourceManager.get().kubeClient().getClient().pods()
                .inNamespace(namespace)
                .withLabel("app.kubernetes.io/instance", name)
                .list()
                .getItems()
                .stream()
                .map(pod -> pod.getStatus())
                .filter(Objects::nonNull)
                .map(status -> status.getContainerStatuses())
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(status -> status.getState())
                .filter(Objects::nonNull)
                .map(state -> state.getWaiting())
                .filter(Objects::nonNull)
                .map(waiting -> waiting.getReason())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
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
