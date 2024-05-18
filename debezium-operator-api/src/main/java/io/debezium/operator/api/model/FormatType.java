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
import io.sundr.builder.annotations.Buildable;

@Documented
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder", lazyCollectionInitEnabled = false)
public class FormatType implements ConfigMappable {

    @JsonPropertyDescription("Format type recognised by Debezium Server.")
    @JsonProperty(defaultValue = "json")
    private String type = "json";

    @JsonPropertyDescription("Format configuration properties.")
    private ConfigProperties config;

    public FormatType() {
        this.config = new ConfigProperties();
    }

    public ConfigProperties getConfig() {
        return config;
    }

    public void setConfig(ConfigProperties config) {
        this.config = config;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public ConfigMapping asConfiguration() {
        return ConfigMapping.empty()
                .putAll(this.config)
                .rootValue(type);
    }
}
