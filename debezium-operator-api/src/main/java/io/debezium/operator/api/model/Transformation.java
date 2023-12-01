/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.debezium.operator.api.config.ConfigMappable;
import io.debezium.operator.api.config.ConfigMapping;
import io.debezium.operator.docs.annotations.Documented;

@Documented
public class Transformation implements ConfigMappable {

    @JsonPropertyDescription("Fully qualified name of Java class implementing the transformation.")
    @JsonProperty(required = true)
    private String type;

    @JsonPropertyDescription("The name of the predicate to be applied to this transformation.")
    private String predicate;

    @JsonPropertyDescription("Determines if the result of the applied predicate will be negated.")
    private boolean negate = false;
    private ConfigProperties config;

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
        config.put("predicate", predicate);
        config.put("negate", negate);
        config.putAll(this.config);
        return config;
    }
}
