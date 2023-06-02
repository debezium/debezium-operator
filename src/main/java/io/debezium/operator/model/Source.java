/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.debezium.operator.config.ConfigMappable;
import io.debezium.operator.config.ConfigMapping;

public class Source implements ConfigMappable {

    private String sourceClass;
    private ConfigProperties config;

    public Source() {
        this.config = new ConfigProperties();
    }

    @JsonProperty("class")
    public String getSourceClass() {
        return sourceClass;
    }

    public void setSourceClass(String clazz) {
        this.sourceClass = clazz;
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
        config.put("connector.class", sourceClass);
        config.putAll(this.config);
        return config;
    }
}
