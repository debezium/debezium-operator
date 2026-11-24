/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.status;

public final class ServerFailedCondition extends Condition {

    public static final String TYPE = ServerRunningCondition.TYPE;
    public static final String MESSAGE_TEMPLATE = "Server %s has failed: %s: %s";
    public static final String MESSAGE_TEMPLATE_NO_DETAIL = "Server %s has failed: %s";

    public ServerFailedCondition(String name, String reason, String detail) {
        super(TYPE, Condition.FALSE, format(name, reason, detail));
    }

    private static String format(String name, String reason, String detail) {
        if (detail == null || detail.isBlank()) {
            return MESSAGE_TEMPLATE_NO_DETAIL.formatted(name, reason);
        }
        return MESSAGE_TEMPLATE.formatted(name, reason, detail);
    }
}
