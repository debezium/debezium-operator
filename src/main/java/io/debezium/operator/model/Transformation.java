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

public class Transformation implements ConfigMappable {

    private String name;
    private String type;
    private String predicate;
    private boolean negate = false;
    private Map<String, Object> props = new HashMap<>(0);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProps(Map<String, Object> props) {
        this.props = props;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public boolean isNegate() {
        return negate;
    }

    public void setNegate(boolean negate) {
        this.negate = negate;
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
        config.rootValue(type);
        config.put("predicate", predicate);
        config.put("negate", negate);
        return config;
    }
}
