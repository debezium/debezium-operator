/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.status;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ServerFailedConditionTest {

    @Test
    void shouldCreateFailureConditionWithDetail() {
        var condition = new ServerFailedCondition("my-server", "CrashLoopBackOff", "Back-off restarting failed container");

        assertThat(condition.getType()).isEqualTo("Running");
        assertThat(condition.getStatus()).isEqualTo("False");
        assertThat(condition.getMessage())
                .isEqualTo("Server my-server has failed: CrashLoopBackOff: Back-off restarting failed container");
    }

    @Test
    void shouldCreateFailureConditionWithoutDetail() {
        var condition = new ServerFailedCondition("my-server", "InvalidImageName", null);

        assertThat(condition.getMessage()).isEqualTo("Server my-server has failed: InvalidImageName");
    }

    @Test
    void shouldCreateFailureConditionWithBlankDetail() {
        var condition = new ServerFailedCondition("my-server", "InvalidImageName", "  ");

        assertThat(condition.getMessage()).isEqualTo("Server my-server has failed: InvalidImageName");
    }
}
