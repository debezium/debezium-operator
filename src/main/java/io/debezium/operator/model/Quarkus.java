/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.debezium.operator.config.ConfigMappable;
import io.debezium.operator.config.ConfigMapping;

public class Quarkus implements ConfigMappable {

    @JsonPropertyDescription("Quarkus configuration properties.")
    private ConfigProperties config;

    public Quarkus() {
        this.config = new ConfigProperties();
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
        config.putAll(this.config);
        return config;
    }
}
