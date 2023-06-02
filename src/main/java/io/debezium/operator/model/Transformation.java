/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.model;

import io.debezium.operator.config.ConfigMappable;
import io.debezium.operator.config.ConfigMapping;

public class Transformation implements ConfigMappable {

    private String type;
    private String predicate;
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
