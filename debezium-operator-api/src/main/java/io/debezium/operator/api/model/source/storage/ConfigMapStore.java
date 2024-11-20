/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.source.storage;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.debezium.operator.api.config.ConfigMapping;
import io.debezium.operator.api.model.DebeziumServer;

public class ConfigMapStore extends AbstractStore {

    public static final String MANAGED_CONFIG_MAP_NAME_TEMPLATE = "%s-offsets";
    public static final String CONFIG_PREFIX = "configmap";

    @JsonPropertyDescription("Name of the offset config map")
    @JsonProperty(required = false)
    private String name;

    public ConfigMapStore(String name, String type) {
        super(CONFIG_PREFIX, type);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected ConfigMapping<DebeziumServer> typeConfiguration(DebeziumServer primary) {

        var configMapName = this.name != null ? this.name : MANAGED_CONFIG_MAP_NAME_TEMPLATE.formatted(primary.getMetadata().getName());

        return ConfigMapping.empty(primary)
                .put("name", configMapName);
    }
}
