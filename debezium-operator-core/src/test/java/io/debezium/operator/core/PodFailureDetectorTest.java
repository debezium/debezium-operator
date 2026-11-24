/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.ContainerStatusBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodConditionBuilder;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.PodStatusBuilder;

class PodFailureDetectorTest {

    @Test
    void shouldDetectCrashLoopBackOff() {
        var pod = podWithContainerStatus(waiting("CrashLoopBackOff", "Back-off restarting failed container"));

        var failure = PodFailureDetector.detect(List.of(pod));

        assertThat(failure).hasValue(new PodFailureDetector.Failure(
                "CrashLoopBackOff", "Back-off restarting failed container"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CrashLoopBackOff",
            "ImagePullBackOff",
            "ErrImagePull",
            "InvalidImageName",
            "CreateContainerConfigError",
            "CreateContainerError",
            "RunContainerError",
            "PreStartHookError",
            "PostStartHookError"
    })
    void shouldDetectTerminalWaitingReason(String reason) {
        var pod = podWithContainerStatus(waiting(reason, "detail"));

        assertThat(PodFailureDetector.detect(List.of(pod)))
                .hasValue(new PodFailureDetector.Failure(reason, "detail"));
    }

    @Test
    void shouldDetectInitContainerFailure() {
        var pod = podWithStatus(new PodStatusBuilder()
                .withPhase("Pending")
                .withInitContainerStatuses(waiting("CreateContainerConfigError", "secret not found"))
                .build());

        assertThat(PodFailureDetector.detect(List.of(pod)))
                .hasValue(new PodFailureDetector.Failure("CreateContainerConfigError", "secret not found"));
    }

    @Test
    void shouldDetectFailedPodPhase() {
        var pod = podWithStatus(new PodStatusBuilder()
                .withPhase("Failed")
                .withReason("Evicted")
                .withMessage("The node was low on resource: memory")
                .build());

        assertThat(PodFailureDetector.detect(List.of(pod)))
                .hasValue(new PodFailureDetector.Failure("Evicted", "The node was low on resource: memory"));
    }

    @Test
    void shouldFallBackToFailedWhenPhaseReasonIsMissing() {
        var pod = podWithStatus(new PodStatusBuilder().withPhase("Failed").build());

        assertThat(PodFailureDetector.detect(List.of(pod)))
                .hasValue(new PodFailureDetector.Failure("Failed", null));
    }

    @Test
    void shouldDetectUnschedulablePod() {
        var pod = podWithStatus(new PodStatusBuilder()
                .withPhase("Pending")
                .withConditions(new PodConditionBuilder()
                        .withType("PodScheduled")
                        .withStatus("False")
                        .withReason("Unschedulable")
                        .withMessage("0/3 nodes are available: 3 Insufficient cpu")
                        .build())
                .build());

        assertThat(PodFailureDetector.detect(List.of(pod)))
                .hasValue(new PodFailureDetector.Failure(
                        "Unschedulable", "0/3 nodes are available: 3 Insufficient cpu"));
    }

    @Test
    void shouldNotDetectNormalRolloutStates() {
        var pending = podWithContainerStatus(waiting("ContainerCreating", null));
        var initializing = podWithContainerStatus(waiting("PodInitializing", null));
        var starting = podWithStatus(new PodStatusBuilder().withPhase("Pending").build());

        assertThat(PodFailureDetector.detect(List.of(pending, initializing, starting))).isEmpty();
    }

    @Test
    void shouldNotDetectHealthyPod() {
        var pod = podWithStatus(new PodStatusBuilder()
                .withPhase("Running")
                .withContainerStatuses(new ContainerStatusBuilder()
                        .withName("server")
                        .withReady(true)
                        .withNewState()
                        .withNewRunning()
                        .endRunning()
                        .endState()
                        .build())
                .build());

        assertThat(PodFailureDetector.detect(List.of(pod))).isEmpty();
    }

    @Test
    void shouldIgnorePodsWithoutStatus() {
        var pod = new PodBuilder().withNewMetadata().withName("no-status").endMetadata().build();

        assertThat(PodFailureDetector.detect(List.of(pod))).isEmpty();
        assertThat(PodFailureDetector.detect(null)).isEmpty();
        assertThat(PodFailureDetector.detect(List.of())).isEmpty();
    }

    @Test
    void shouldIgnoreStaleFailedPodWhenNewerPodIsHealthy() {
        var staleFailed = podWithStatus(new PodStatusBuilder()
                .withPhase("Failed")
                .withReason("Evicted")
                .build(), "2026-11-19T10:00:00Z");
        var healthy = podWithStatus(new PodStatusBuilder()
                .withPhase("Running")
                .build(), "2026-11-19T10:05:00Z");

        assertThat(PodFailureDetector.detect(List.of(staleFailed, healthy))).isEmpty();
    }

    @Test
    void shouldReportFailureOfTheNewestPod() {
        var oldHealthy = podWithStatus(new PodStatusBuilder()
                .withPhase("Running")
                .build(), "2026-11-19T10:00:00Z");
        var failing = podWithStatus(new PodStatusBuilder()
                .withPhase("Running")
                .withContainerStatuses(waiting("CrashLoopBackOff", "Back-off restarting failed container"))
                .build(), "2026-11-19T10:05:00Z");

        assertThat(PodFailureDetector.detect(List.of(oldHealthy, failing)))
                .hasValue(new PodFailureDetector.Failure(
                        "CrashLoopBackOff", "Back-off restarting failed container"));
    }

    @Test
    void shouldIgnoreTerminatingPod() {
        var terminating = new PodBuilder()
                .withNewMetadata()
                .withName("terminating")
                .withCreationTimestamp("2026-11-19T10:00:00Z")
                .withDeletionTimestamp("2026-11-19T10:06:00Z")
                .endMetadata()
                .withStatus(new PodStatusBuilder()
                        .withPhase("Running")
                        .withContainerStatuses(waiting("CrashLoopBackOff", "detail"))
                        .build())
                .build();

        assertThat(PodFailureDetector.detect(List.of(terminating))).isEmpty();
    }

    @Test
    void shouldIgnoreSucceededPod() {
        var succeeded = podWithStatus(new PodStatusBuilder()
                .withPhase("Succeeded")
                .build());

        assertThat(PodFailureDetector.detect(List.of(succeeded))).isEmpty();
    }

    private static Pod podWithContainerStatus(ContainerStatus status) {
        return podWithStatus(new PodStatusBuilder()
                .withPhase("Running")
                .withContainerStatuses(status)
                .build());
    }

    private static Pod podWithStatus(PodStatus status) {
        return podWithStatus(status, "2026-11-19T10:00:00Z");
    }

    private static Pod podWithStatus(PodStatus status, String creationTimestamp) {
        return new PodBuilder()
                .withNewMetadata()
                .withName("test-server")
                .withCreationTimestamp(creationTimestamp)
                .endMetadata()
                .withStatus(status)
                .build();
    }

    private static ContainerStatus waiting(String reason, String message) {
        return new ContainerStatusBuilder()
                .withName("server")
                .withNewState()
                .withNewWaiting()
                .withReason(reason)
                .withMessage(message)
                .endWaiting()
                .endState()
                .build();
    }
}
