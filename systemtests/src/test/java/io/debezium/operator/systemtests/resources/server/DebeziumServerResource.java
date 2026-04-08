/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.systemtests.resources.server;

import static io.debezium.operator.systemtests.ConfigProperties.FABRIC8_POLL_INTERVAL;
import static io.debezium.operator.systemtests.ConfigProperties.FABRIC8_POLL_TIMEOUT;
import static org.awaitility.Awaitility.await;

import java.io.InputStream;
import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.util.Clock;
import io.debezium.util.ElapsedTimeStrategy;
import io.debezium.util.Stopwatch;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperationsImpl;
import io.skodjob.testframe.interfaces.ResourceType;
import io.skodjob.testframe.resources.KubeResourceManager;

public class DebeziumServerResource implements ResourceType<DebeziumServer> {

    private static final long DIAGNOSTICS_INTERVAL = 60_000;
    private static final int LOGS_LINES = 100;
    private final MixedOperation<DebeziumServer, DebeziumServerList, Resource<DebeziumServer>> client;
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private final Map<String, Stopwatch> waitStopwatches = new java.util.HashMap<>();
    private final Map<String, ElapsedTimeStrategy> diagnosticsTimers = new java.util.HashMap<>();

    public DebeziumServerResource() {
        this.client = KubeResourceManager.getKubeClient().getClient().resources(DebeziumServer.class, DebeziumServerList.class);
    }

    public DebeziumServer get(String namespace, String name) {
        return client.inNamespace(namespace).withName(name).get();
    }

    @Override
    public NonNamespaceOperation<?, ?, ?> getClient() {
        return client;
    }

    @Override
    public String getKind() {
        return "DebeziumServer";
    }

    @Override
    public void create(DebeziumServer debeziumServer) {
        client.inNamespace(debeziumServer.getMetadata().getNamespace()).resource(debeziumServer).create();
    }

    @Override
    public void update(DebeziumServer debeziumServer) {
        client.inNamespace(debeziumServer.getMetadata().getNamespace()).resource(debeziumServer).update();
    }

    @Override
    public void delete(String name) {
        client.list().getItems().stream()
                .filter(n -> n.getMetadata().getName().equals(name)).findFirst().ifPresent(client::delete);
    }

    @Override
    public void replace(String s, Consumer<DebeziumServer> editor) {
        DebeziumServer toBeReplaced = client.withName(s).get();
        editor.accept(toBeReplaced);
        update(toBeReplaced);
    }

    @Override
    public boolean waitForReadiness(DebeziumServer debeziumServer) {
        String resourceKey = debeziumServer.getMetadata().getNamespace() + "/" + debeziumServer.getMetadata().getName();

        // Initialize tracking for this resource (persists across multiple waitForReadiness calls)
        Stopwatch stopwatch = waitStopwatches.computeIfAbsent(resourceKey, k -> Stopwatch.reusable().start());
        ElapsedTimeStrategy diagnosticsTimer = diagnosticsTimers.computeIfAbsent(resourceKey,
                k -> ElapsedTimeStrategy.constant(Clock.SYSTEM, Duration.ofMillis(DIAGNOSTICS_INTERVAL)));

        try {
            await().atMost(Duration.ofSeconds(FABRIC8_POLL_TIMEOUT)).pollInterval(Duration.ofSeconds(FABRIC8_POLL_INTERVAL))
                    .until(() -> {
                        DebeziumServer dbzServer = client.inNamespace(debeziumServer.getMetadata().getNamespace())
                                .withName(debeziumServer.getMetadata().getName()).get();

                        boolean ready = dbzServer != null && dbzServer.getStatus() != null && dbzServer.getStatus().getConditions() != null
                                && dbzServer.getStatus().getConditions().stream()
                                        .anyMatch(condition -> condition.getType().equals("Ready") && condition.getStatus().equals("True"));

                        if (ready) {
                            stopwatch.stop();
                            // Clean up tracking on success
                            waitStopwatches.remove(resourceKey);
                            diagnosticsTimers.remove(resourceKey);
                            return true;
                        }
                        else {
                            // Check if diagnostics should be triggered
                            if (diagnosticsTimer.hasElapsed()) {
                                Duration elapsed = stopwatch.durations().statistics().getTotal();
                                logger.warn("Triggering diagnostics after {} seconds", elapsed.toSeconds());
                                logDiagnostics(debeziumServer);
                            }

                            logger.info("Waiting for readiness of Debezium Server...");
                            return false;
                        }
                    });
            return true;
        }
        catch (Exception e) {
            // Don't clean up on timeout/exception - we want to preserve state across retries
            throw e;
        }
    }

    private void logDiagnostics(DebeziumServer debeziumServer) {
        String namespace = debeziumServer.getMetadata().getNamespace();
        String name = debeziumServer.getMetadata().getName();

        logger.warn("DebeziumServer {}/{} not ready after 60s, collecting diagnostics...", namespace, name);

        try {
            DebeziumServer dbzServer = client.inNamespace(namespace).withName(name).get();
            if (dbzServer != null && dbzServer.getStatus() != null && dbzServer.getStatus().getConditions() != null) {
                logger.warn("DebeziumServer status conditions:");
                dbzServer.getStatus().getConditions().forEach(condition -> logger.warn("  - Type: {}, Status: {}, Message: {}",
                        condition.getType(), condition.getStatus(), condition.getMessage()));
            }

            // Log pod status
            var pods = KubeResourceManager.getKubeClient().getClient().pods()
                    .inNamespace(namespace)
                    .withLabel("app.kubernetes.io/instance", name)
                    .list().getItems();

            if (pods.isEmpty()) {
                logger.warn("No pods found for DebeziumServer {}/{}", namespace, name);
            }
            else {
                pods.forEach(pod -> {
                    logger.warn("Pod {}: Phase={}, Reason={}",
                            pod.getMetadata().getName(),
                            pod.getStatus().getPhase(),
                            pod.getStatus().getReason());

                    if (pod.getStatus().getContainerStatuses() != null) {
                        pod.getStatus().getContainerStatuses().forEach(cs -> {
                            logger.warn("  Container {}: Ready={}, RestartCount={}",
                                    cs.getName(), cs.getReady(), cs.getRestartCount());
                            if (cs.getState() != null) {
                                if (cs.getState().getWaiting() != null) {
                                    logger.warn("    Waiting: {}", cs.getState().getWaiting().getReason());
                                }
                                if (cs.getState().getTerminated() != null) {
                                    logger.warn("    Terminated: {}", cs.getState().getTerminated().getReason());
                                }
                            }
                        });
                    }

                    var events = KubeResourceManager.getKubeClient().getClient().v1().events()
                            .inNamespace(namespace)
                            .withField("involvedObject.name", pod.getMetadata().getName())
                            .list().getItems();

                    if (!events.isEmpty()) {
                        logger.warn("  Recent events for pod {}:", pod.getMetadata().getName());
                        events.stream()
                                .filter(e -> e.getLastTimestamp() != null)
                                .sorted((e1, e2) -> e2.getLastTimestamp().compareTo(e1.getLastTimestamp()))
                                .limit(5)
                                .forEach(event -> logger.warn("    [{}] {}: {}",
                                        event.getType(), event.getReason(), event.getMessage()));
                    }

                    if (pod.getStatus().getContainerStatuses() != null) {
                        pod.getStatus().getContainerStatuses().forEach(cs -> {
                            // Only get logs if container has been running or terminated (not waiting for image pull)
                            if (cs.getState() != null &&
                                    (cs.getState().getRunning() != null || cs.getState().getTerminated() != null ||
                                            (cs.getState().getWaiting() != null &&
                                                    cs.getState().getWaiting().getReason() != null &&
                                                    !cs.getState().getWaiting().getReason().contains("Image")))) {

                                try {
                                    logger.warn("  Recent logs from container {}:", cs.getName());
                                    String logs = KubeResourceManager.getKubeClient().getClient().pods()
                                            .inNamespace(namespace)
                                            .withName(pod.getMetadata().getName())
                                            .inContainer(cs.getName())
                                            .tailingLines(LOGS_LINES)
                                            .getLog();

                                    if (logs != null && !logs.isEmpty()) {
                                        String[] logLines = logs.split("\n");
                                        for (String logLine : logLines) {
                                            logger.warn("      {}", logLine);
                                        }
                                    }
                                    else {
                                        logger.warn("      (no logs available)");
                                    }
                                }
                                catch (Exception e) {
                                    logger.debug("Could not retrieve logs for container {}: {}", cs.getName(), e.getMessage());
                                }
                            }
                        });
                    }
                });
            }
        }
        catch (Exception e) {
            logger.error("Failed to collect diagnostics", e);
        }
    }

    @Override
    public boolean waitForDeletion(DebeziumServer debeziumServer) {
        if (debeziumServer == null) {
            return true;
        }

        String namespace = debeziumServer.getMetadata().getNamespace();
        String name = debeziumServer.getMetadata().getName();

        Stopwatch stopwatch = Stopwatch.reusable().start();
        ElapsedTimeStrategy diagnosticsTimer = ElapsedTimeStrategy.constant(Clock.SYSTEM, Duration.ofSeconds(30));
        ElapsedTimeStrategy forceDeleteTimer = ElapsedTimeStrategy.constant(Clock.SYSTEM, Duration.ofSeconds(120));
        final boolean[] diagnosticsLogged = { false };

        try {
            await().atMost(Duration.ofSeconds(180)).pollInterval(Duration.ofSeconds(2))
                    .until(() -> {
                        DebeziumServer resource = client.inNamespace(namespace).withName(name).get();
                        if (resource == null) {
                            stopwatch.stop();
                            return true;
                        }

                        // Log diagnostics after 30s (only once)
                        if (!diagnosticsLogged[0] && diagnosticsTimer.hasElapsed()) {
                            logDeletionDiagnostics(namespace, name, resource);
                            diagnosticsLogged[0] = true;
                        }

                        // Force delete after 2 minutes
                        if (forceDeleteTimer.hasElapsed()) {
                            Duration elapsed = stopwatch.durations().statistics().getTotal();
                            logger.warn("DebeziumServer {}/{} stuck in deletion for {}s, attempting force delete",
                                    namespace, name, elapsed.toSeconds());
                            forceDelete(namespace, name);
                            return false; // Continue waiting after force delete
                        }

                        logger.info("Waiting for deletion of DebeziumServer {}/{}...", namespace, name);
                        return false;
                    });
            return true;
        }
        catch (Exception e) {
            stopwatch.stop();
            logger.error("Error waiting for deletion of DebeziumServer {}/{}", namespace, name, e);
            return false;
        }
    }

    private void logDeletionDiagnostics(String namespace, String name, DebeziumServer resource) {
        logger.warn("DebeziumServer {}/{} deletion is taking longer than expected", namespace, name);

        try {
            // Check finalizers
            if (resource.getMetadata().getFinalizers() != null && !resource.getMetadata().getFinalizers().isEmpty()) {
                logger.warn("Resource has finalizers: {}", resource.getMetadata().getFinalizers());
            }

            // Check deletion timestamp
            logger.warn("Deletion timestamp: {}", resource.getMetadata().getDeletionTimestamp());

            // Check owned resources
            logger.warn("Checking for owned resources that might block deletion...");

            // Check deployment
            var deployment = KubeResourceManager.getKubeClient().getClient().apps().deployments()
                    .inNamespace(namespace).withName(name).get();
            if (deployment != null) {
                logger.warn("  Deployment still exists: {}", deployment.getMetadata().getName());
                if (deployment.getMetadata().getDeletionTimestamp() != null) {
                    logger.warn("    Deployment is being deleted since: {}", deployment.getMetadata().getDeletionTimestamp());
                }
            }

            // Check pods
            var pods = KubeResourceManager.getKubeClient().getClient().pods()
                    .inNamespace(namespace)
                    .withLabel("app.kubernetes.io/instance", name)
                    .list().getItems();
            if (!pods.isEmpty()) {
                logger.warn("  {} pod(s) still exist:", pods.size());
                pods.forEach(pod -> {
                    logger.warn("    - {}: Phase={}, DeletionTimestamp={}",
                            pod.getMetadata().getName(),
                            pod.getStatus().getPhase(),
                            pod.getMetadata().getDeletionTimestamp());
                });
            }
        }
        catch (Exception e) {
            logger.error("Failed to collect deletion diagnostics", e);
        }
    }

    private void forceDelete(String namespace, String name) {
        try {
            logger.warn("Force deleting DebeziumServer {}/{}", namespace, name);
            client.inNamespace(namespace).withName(name).withGracePeriod(0).delete();

            // Also try to remove finalizers if present
            try {
                var resource = client.inNamespace(namespace).withName(name).get();
                if (resource != null && resource.getMetadata().getFinalizers() != null) {
                    logger.warn("Removing finalizers from DebeziumServer {}/{}", namespace, name);
                    resource.getMetadata().setFinalizers(null);
                    client.inNamespace(namespace).resource(resource).update();
                }
            }
            catch (Exception e) {
                logger.debug("Could not remove finalizers (resource may already be deleted)", e);
            }
        }
        catch (Exception e) {
            logger.error("Failed to force delete DebeziumServer {}/{}", namespace, name, e);
        }
    }

    public DebeziumServer loadResource(InputStream is) {
        return (DebeziumServer) ((HasMetadataOperationsImpl<?, ?>) this.client.load(is)).getItem();
    }
}
