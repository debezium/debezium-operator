/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.status;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DebeziumServerStatusTest {

    private DebeziumServerStatus debeziumServerStatus;
    private Condition conditionOne;
    private Condition conditionTwo;

    @BeforeEach
    void before() {
        debeziumServerStatus = new DebeziumServerStatus();

        conditionOne = new Condition();
        conditionOne.setMessage("True");
        conditionOne.setType("Server Debezium is ready");
        conditionOne.setStatus("Ready");

        conditionTwo = new Condition();
        conditionTwo.setMessage("False");
        conditionTwo.setType("Server Debezium is not ready");
        conditionTwo.setStatus("Not Ready");
    }

    @Test
    void shouldReturnAddedConditions() {
        debeziumServerStatus.addToConditions(conditionOne);
        debeziumServerStatus.addToConditions(conditionTwo);

        assertThat(debeziumServerStatus.getConditions()).hasSize(2).containsExactly(conditionOne, conditionTwo);
    }

    @Test
    void shouldReturnSetConditions() {
        List<Condition> conditions = new ArrayList<>();
        conditions.add(conditionOne);
        conditions.add(conditionTwo);
        debeziumServerStatus.setConditions(conditions);

        assertThat(debeziumServerStatus.getConditions()).hasSize(2).containsExactly(conditionOne, conditionTwo);
    }
}
