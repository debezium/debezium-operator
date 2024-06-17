/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.status;

public class ServerStoppedCondition extends Condition {
    public static final String TYPE = "Running";
    public static final String MESSAGE_TEMPLATE = "Server %s is stopped";

    public ServerStoppedCondition(String name) {
        super(TYPE, Condition.FALSE, MESSAGE_TEMPLATE.formatted(name));
    }
}
