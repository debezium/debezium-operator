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

import io.debezium.operator.config.ConfigMappable;
import io.debezium.operator.config.ConfigMapping;

public class FormatType implements ConfigMappable {

    private String type;

    private final Map<String, Object> props;

    public FormatType() {
        type = "json";
        props = new HashMap<>(0);
    }

    @JsonAnyGetter
    public Map<String, Object> getProps() {
        return props;
    }

    @JsonAnySetter
    public void setProps(String name, Object value) {
        getProps().put(name, value);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public ConfigMapping asConfiguration() {
        var config = ConfigMapping.from(props);
        config.rootValue(type);
        return config;
    }
}
