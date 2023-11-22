/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.status;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConditionTest {

    private Condition condition;
    private String STATUS = "True";
    private String MESSAGE = "Server Debezium is ready";
    private String TYPE = "Ready";

    @BeforeEach
    void before() {
        condition = new Condition();
    }

    @Test
    void getStatusShouldReturnSetValue() {
        condition.setStatus(STATUS);
        assertThat(condition.getStatus()).isEqualTo(STATUS);
    }

    @Test
    void getMessageShouldReturnSetValue() {
        condition.setMessage(MESSAGE);
        assertThat(condition.getMessage()).isEqualTo(MESSAGE);
    }

    @Test
    void getTypeShouldReturnSetValue() {
        condition.setType(TYPE);
        assertThat(condition.getType()).isEqualTo(TYPE);
    }
}
