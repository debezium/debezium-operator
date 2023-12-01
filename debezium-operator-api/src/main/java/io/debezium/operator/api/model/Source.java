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
public class Source implements ConfigMappable {

    @JsonPropertyDescription("Fully qualified name of source connector Java class.")
    @JsonProperty(value = "class", required = true)
    private String sourceClass;

    @JsonPropertyDescription("Source connector configuration properties.")
    private ConfigProperties config;

    public Source() {
        this.config = new ConfigProperties();
    }

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
