/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.api.model.DebeziumServerBuilder;
import io.debezium.operator.api.model.DebeziumServerSpecBuilder;

public class RuntimeConfigTest {

    static DebeziumServer server;

    @BeforeAll
    static void setup() {
        server = new DebeziumServerBuilder()
                .withSpec(new DebeziumServerSpecBuilder()
                        .withRuntime(new RuntimeBuilder()
                                .withApi(new RuntimeApiBuilder()
                                        .withEnabled(true)
                                        .build())
                                .build())
                        .build())
                .build();

    }

    @Test
    void shouldHaveEnableApiProperty() {
        assertThat(server.asConfiguration().getAsMapSimple())
                .containsEntry("debezium.api.enabled", "true");
    }
}
