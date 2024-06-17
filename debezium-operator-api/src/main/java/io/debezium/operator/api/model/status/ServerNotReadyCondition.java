/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.status;

public final class ServerNotReadyCondition extends Condition {

    public static final String MESSAGE_TEMPLATE = "Server %s deployment in progress";

    public ServerNotReadyCondition(String name) {
        super(ServerReadyCondition.TYPE, Condition.FALSE, MESSAGE_TEMPLATE.formatted(name));
    }
}
