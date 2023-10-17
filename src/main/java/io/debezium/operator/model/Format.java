/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.debezium.operator.config.ConfigMappable;
import io.debezium.operator.config.ConfigMapping;

public class Format implements ConfigMappable {

    @JsonPropertyDescription("Message key format configuration.")
    private FormatType key;

    @JsonPropertyDescription("Message value format configuration.")
    private FormatType value;

    @JsonPropertyDescription("Message header format configuration.")
    private FormatType header;

    public Format() {
        this.key = new FormatType();
        this.value = new FormatType();
        this.header = new FormatType();
    }

    public FormatType getKey() {
        return key;
    }

    public void setKey(FormatType key) {
        this.key = key;
    }

    public FormatType getValue() {
        return value;
    }

    public void setValue(FormatType value) {
        this.value = value;
    }

    public FormatType getHeader() {
        return header;
    }

    public void setHeader(FormatType header) {
        this.header = header;
    }

    @Override
    public ConfigMapping asConfiguration() {
        var config = ConfigMapping.empty();
        config.putAll("key", key);
        config.putAll("value", value);
        config.putAll("header", header);
        return config;
    }
}
