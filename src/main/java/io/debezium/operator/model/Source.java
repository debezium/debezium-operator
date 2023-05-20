/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.debezium.operator.config.ConfigMappable;
import io.debezium.operator.config.ConfigMapping;

public class Source implements ConfigMappable {

    private String sourceClass;
    private Map<String, Object> props = new HashMap<>(0);

    @JsonProperty("class")
    public String getSourceClass() {
        return sourceClass;
    }

    public void setSourceClass(String clazz) {
        this.sourceClass = clazz;
    }

    @JsonAnyGetter
    public Map<String, Object> getProps() {
        return props;
    }

    @JsonAnySetter
    public void setProps(String name, Object value) {
        getProps().put(name, value);
    }

    @Override
    public ConfigMapping asConfiguration() {
        var config = ConfigMapping.from(props);
        config.put("connector.class", sourceClass);
        return config;
    }
}
