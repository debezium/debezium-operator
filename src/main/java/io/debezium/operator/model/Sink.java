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

public class Sink implements ConfigMappable {

    private String type;
    private Map<String, Object> props = new HashMap<>(0);

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
        var config = ConfigMapping.empty();
        config.put("type", type, props);
        return config;
    }
}
