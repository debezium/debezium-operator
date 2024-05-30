/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.source.storage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.debezium.operator.api.config.ConfigMapping;
import io.debezium.operator.api.model.ConfigProperties;

public abstract class AbstractStore implements Store {
    @JsonIgnore
    private final String type;

    @JsonPropertyDescription("Additional store configuration properties.")
    private ConfigProperties config = new ConfigProperties();

    public AbstractStore(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public ConfigProperties getConfig() {
        return config;
    }

    public void setConfig(ConfigProperties config) {
        this.config = config;
    }

    @Override
    public ConfigMapping asConfiguration() {
        return ConfigMapping.empty()
                .putAll(config)
                .rootValue(type);

    }
}
