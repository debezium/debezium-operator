/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.debezium.operator.api.config.ConfigMappable;
import io.debezium.operator.api.config.ConfigMapping;
import io.debezium.operator.docs.annotations.Documented;

@Documented(hidden = true, name = "Map")
public class ConfigProperties implements ConfigMappable {

    private Map<String, Object> props = new HashMap<>(0);

    @JsonAnyGetter
    public Map<String, Object> getProps() {
        return props;
    }

    @JsonIgnore
    public void setAllProps(Map<String, Object> props) {
        this.props.putAll(props);
    }

    @JsonAnySetter
    public void setProps(String name, Object value) {
        getProps().put(name, value);
    }

    @Override
    public ConfigMapping asConfiguration() {
        return ConfigMapping.from(props);
    }
}
