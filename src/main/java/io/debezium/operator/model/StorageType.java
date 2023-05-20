/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum StorageType {
    @JsonProperty("ephemeral")
    EPHEMERAL,
    @JsonProperty("persistent")
    PERSISTENT,
    ;
}
