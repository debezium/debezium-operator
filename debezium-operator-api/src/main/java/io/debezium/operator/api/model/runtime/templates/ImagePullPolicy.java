/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.runtime.templates;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.debezium.operator.docs.annotations.Documented;

@Documented(hidden = true)
public enum ImagePullPolicy {
    @JsonProperty("Always")
    ALWAYS("Always"),
    @JsonProperty("IfNotPresent")
    IF_NOT_PRESENT("IfNotPresent"),
    @JsonProperty("Never")
    NEVER("Never");

    private final String value;

    ImagePullPolicy(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
