/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodCondition;
import io.fabric8.kubernetes.api.model.PodStatus;

/**
 * Detects terminal pod failures that should be surfaced in the {@code DebeziumServer} status.
 * <p>
 * Only states that require user intervention (or at least will not resolve by themselves during
 * a normal rollout) are considered failures. In particular {@code Pending},
 * {@code ContainerCreating} and {@code PodInitializing} are part of a normal deployment and are
 * not reported.
 * <p>
 * Detection is intentionally limited to information Kubernetes exposes about the pod. Failures
 * inside the Debezium engine that leave the container running and ready (for example a sink that
 * exhausts its retries and stops the engine) are not observable here: Debezium Server's health
 * endpoint does not reflect engine state. Such failures need to be addressed in the server by
 * failing the readiness probe or the process itself.
 */
public final class PodFailureDetector {

    public static final Set<String> TERMINAL_WAITING_REASONS = Set.of(
            "CrashLoopBackOff",
            "ImagePullBackOff",
            "ErrImagePull",
            "InvalidImageName",
            "CreateContainerConfigError",
            "CreateContainerError",
            "RunContainerError",
            "PreStartHookError",
            "PostStartHookError");

    private PodFailureDetector() {
    }

    /**
     * A detected pod failure.
     *
     * @param reason  Kubernetes failure reason, e.g. {@code CrashLoopBackOff}
     * @param message human-readable detail provided by Kubernetes, may be {@code null}
     */
    public record Failure(String reason, String message) {
    }

    /**
     * Returns the failure of the pod that currently represents the Debezium Server.
     * <p>
     * The server is deployed with at most one replica, but the label selector can transiently
     * match more than one pod (e.g. a terminating pod plus its replacement, or a leftover failed
     * pod waiting for garbage collection). Only the newest non-terminating pod is inspected so a
     * stale pod cannot make a healthy server look failed.
     *
     * @param pods pods belonging to the Debezium Server
     * @return detected failure or empty if the current pod is healthy, still starting or absent
     */
    public static Optional<Failure> detect(List<Pod> pods) {
        if (pods == null) {
            return Optional.empty();
        }
        return pods.stream()
                .filter(PodFailureDetector::isActive)
                .max(Comparator.comparing(PodFailureDetector::creationTimestamp,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .flatMap(PodFailureDetector::detect);
    }

    private static boolean isActive(Pod pod) {
        if (pod == null || pod.getMetadata() == null || pod.getMetadata().getDeletionTimestamp() != null) {
            return false;
        }
        var status = pod.getStatus();
        return status != null && !"Succeeded".equals(status.getPhase());
    }

    private static String creationTimestamp(Pod pod) {
        return pod.getMetadata().getCreationTimestamp();
    }

    private static Optional<Failure> detect(Pod pod) {
        PodStatus status = pod == null ? null : pod.getStatus();
        if (status == null) {
            return Optional.empty();
        }

        if ("Failed".equals(status.getPhase())) {
            return Optional.of(new Failure(reasonOr(status.getReason(), "Failed"), status.getMessage()));
        }

        var failure = detectContainerFailures(status.getContainerStatuses());
        if (failure.isEmpty()) {
            failure = detectContainerFailures(status.getInitContainerStatuses());
        }
        if (failure.isPresent()) {
            return failure;
        }

        return detectUnschedulable(status.getConditions());
    }

    private static Optional<Failure> detectContainerFailures(List<ContainerStatus> statuses) {
        if (statuses == null) {
            return Optional.empty();
        }
        for (ContainerStatus containerStatus : statuses) {
            if (containerStatus == null || containerStatus.getState() == null || containerStatus.getState().getWaiting() == null) {
                continue;
            }
            var waiting = containerStatus.getState().getWaiting();
            if (TERMINAL_WAITING_REASONS.contains(waiting.getReason())) {
                return Optional.of(new Failure(waiting.getReason(), waiting.getMessage()));
            }
        }
        return Optional.empty();
    }

    private static Optional<Failure> detectUnschedulable(List<PodCondition> conditions) {
        if (conditions == null) {
            return Optional.empty();
        }
        for (PodCondition condition : conditions) {
            if (condition != null
                    && "PodScheduled".equals(condition.getType())
                    && "False".equals(condition.getStatus())
                    && "Unschedulable".equals(condition.getReason())) {
                return Optional.of(new Failure(condition.getReason(), condition.getMessage()));
            }
        }
        return Optional.empty();
    }

    private static String reasonOr(String reason, String fallback) {
        return reason == null || reason.isBlank() ? fallback : reason;
    }
}
