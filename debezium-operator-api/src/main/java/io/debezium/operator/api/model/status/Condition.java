/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.status;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.debezium.operator.docs.annotations.Documented;

@Documented
public class Condition {

    @JsonPropertyDescription("The status of the condition, either True, False or Unknown.")
    private String status;
    @JsonPropertyDescription("Human-readable message indicating details about the condition’s last transition.")
    private String message;
    @JsonPropertyDescription("Unique identifier of a condition.")
    private String type;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
