/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.debezium.operator.config.ConfigMappable;
import io.debezium.operator.config.ConfigMapping;

public class Predicate implements ConfigMappable {

    @JsonPropertyDescription("Fully qualified name of Java class implementing the predicate.")
    @JsonProperty(required = true)
    private String type;

    @JsonPropertyDescription("Predicate configuration properties.")
    private ConfigProperties config;

    public Predicate() {
        this.config = new ConfigProperties();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ConfigProperties getConfig() {
        return config;
    }

    public void setConfig(ConfigProperties config) {
        this.config = config;
    }

    @Override
    public ConfigMapping asConfiguration() {
        var config = ConfigMapping.empty();
        config.put("type", type);
        config.putAll(this.config);
        return config;

    }
}
