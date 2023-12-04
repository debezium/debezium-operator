/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.docs.model;

public record FieldDescription(
        String name,
        String type,
        String typeRef,
        String defaultVal,
        String description) {

    public FieldDescription(
            String name,
            String type,
            String defaultVal,
            String description) {
        this(name, type, null, defaultVal, description);
    }

}
