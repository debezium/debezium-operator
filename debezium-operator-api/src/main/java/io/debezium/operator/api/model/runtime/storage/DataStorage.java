/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.runtime.storage;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.debezium.operator.docs.annotations.Documented;

@Documented
public class DataStorage {

    @JsonPropertyDescription("Storage type.")
    @JsonProperty(defaultValue = "ephemeral")
    private StorageType type;

    @JsonPropertyDescription("Name of persistent volume claim for persistent storage.")
    private String claimName;

    public DataStorage() {
        this.type = StorageType.EPHEMERAL;
    }

    public StorageType getType() {
        return type;
    }

    public void setType(StorageType type) {
        this.type = type;
    }

    public String getClaimName() {
        return claimName;
    }

    public void setClaimName(String claimName) {
        this.claimName = claimName;
    }
}
