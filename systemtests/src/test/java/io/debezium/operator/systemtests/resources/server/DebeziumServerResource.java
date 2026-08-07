/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.systemtests.resources.server;

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
import io.skodjob.kubetest4j.interfaces.ResourceType;
import io.skodjob.kubetest4j.resources.KubeResourceManager;

public class DebeziumServerResource implements ResourceType<DebeziumServer> {

    private static final long DIAGNOSTICS_INTERVAL = 60_000;
    private static final int LOGS_LINES = 100;
    private final MixedOperation<DebeziumServer, DebeziumServerList, Resource<DebeziumServer>> client;
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private final Map<String, Stopwatch> waitStopwatches = new java.util.HashMap<>();
    private final Map<String, ElapsedTimeStrategy> diagnosticsTimers = new java.util.HashMap<>();

    public DebeziumServerResource() {
        this.client = KubeResourceManager.get().kubeClient().getClient().resources(DebeziumServer.class, DebeziumServerList.class);
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
    public void delete(DebeziumServer debeziumServer) {
        client.inNamespace(debeziumServer.getMetadata().getNamespace())
                .withName(debeziumServer.getMetadata().getName()).delete();
    }

    @Override
    public void replace(DebeziumServer debeziumServer, Consumer<DebeziumServer> editor) {
        editor.accept(debeziumServer);
        update(debeziumServer);
    }

    @Override
    public boolean isReady(DebeziumServer debeziumServer) {
        String resourceKey = debeziumServer.getMetadata().getNamespace() + "/" + debeziumServer.getMetadata().getName();

        Stopwatch stopwatch = waitStopwatches.computeIfAbsent(resourceKey, k -> Stopwatch.reusable().start());
        ElapsedTimeStrategy diagnosticsTimer = diagnosticsTimers.computeIfAbsent(resourceKey,
                k -> ElapsedTimeStrategy.constant(Clock.SYSTEM, Duration.ofMillis(DIAGNOSTICS_INTERVAL)));

        DebeziumServer dbzServer = client.inNamespace(debeziumServer.getMetadata().getNamespace())
                .withName(debeziumServer.getMetadata().getName()).get();

        boolean ready = dbzServer != null && dbzServer.getStatus() != null && dbzServer.getStatus().getConditions() != null
                && dbzServer.getStatus().getObservedGeneration() != null
                && dbzServer.getStatus().getObservedGeneration() >= dbzServer.getMetadata().getGeneration()
                && dbzServer.getStatus().getConditions().stream()
                        .anyMatch(condition -> condition.getType().equals("Ready") && condition.getStatus().equals("True"));

        if (ready) {
            stopwatch.stop();
            waitStopwatches.remove(resourceKey);
            diagnosticsTimers.remove(resourceKey);
            return true;
        }

        if (diagnosticsTimer.hasElapsed()) {
            Duration elapsed = stopwatch.durations().statistics().getTotal();
            logger.warn("Triggering diagnostics after {} seconds", elapsed.toSeconds());
            logDiagnostics(debeziumServer);
        }

        logger.info("Waiting for readiness of Debezium Server...");
        return false;
    }

    @Override
    public boolean isDeleted(DebeziumServer debeziumServer) {
        if (debeziumServer == null) {
            return true;
        }
        return client.inNamespace(debeziumServer.getMetadata().getNamespace())
                .withName(debeziumServer.getMetadata().getName()).get() == null;
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
            var pods = KubeResourceManager.get().kubeClient().getClient().pods()
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

                    var events = KubeResourceManager.get().kubeClient().getClient().v1().events()
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
                                    String logs = KubeResourceManager.get().kubeClient().getClient().pods()
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
}
