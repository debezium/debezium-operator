/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.debezium.doc.FixFor;
import io.debezium.operator.api.model.DebeziumServer;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;

class DebeziumServerUpdateFilterTest {

    private final DebeziumServerUpdateFilter filter = new DebeziumServerUpdateFilter();

    private static DebeziumServer server(long generation, boolean stopped) {
        var server = new DebeziumServer();
        var metadata = new ObjectMetaBuilder()
                .withName("test-ds")
                .withGeneration(generation)
                .withAnnotations(stopped ? Map.of("debezium.io/stop", "true") : Map.of())
                .build();
        server.setMetadata(metadata);
        return server;
    }

    @Test
    @FixFor("debezium/dbz#2528")
    void shouldAcceptWhenGenerationChanged() {
        assertThat(filter.accept(server(2, false), server(1, false))).isTrue();
    }

    @Test
    @FixFor("debezium/dbz#2528")
    void shouldAcceptWhenStopAnnotationAdded() {
        assertThat(filter.accept(server(1, true), server(1, false))).isTrue();
    }

    @Test
    @FixFor("debezium/dbz#2528")
    void shouldAcceptWhenStopAnnotationRemoved() {
        assertThat(filter.accept(server(1, false), server(1, true))).isTrue();
    }

    @Test
    @FixFor("debezium/dbz#2528")
    void shouldRejectWhenNothingRelevantChanged() {
        assertThat(filter.accept(server(1, false), server(1, false))).isFalse();
    }

    @Test
    @FixFor("debezium/dbz#2528")
    void shouldRejectWhenOnlyStatusChangedWhileStopped() {
        // Same generation and same stop flag: e.g. the operator's own status patch must not
        // re-trigger reconciliation, avoiding a loop.
        assertThat(filter.accept(server(1, true), server(1, true))).isFalse();
    }
}
