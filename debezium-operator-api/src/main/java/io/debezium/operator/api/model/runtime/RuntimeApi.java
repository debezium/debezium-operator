/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.runtime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.debezium.operator.api.config.ConfigMappable;
import io.debezium.operator.api.config.ConfigMapping;
import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.docs.annotations.Documented;
import io.sundr.builder.annotations.Buildable;

@JsonPropertyOrder({ "enabled" })
@Documented
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder", lazyCollectionInitEnabled = false)
public class RuntimeApi implements ConfigMappable<DebeziumServer> {

    @JsonPropertyDescription("Whether the API should be enabled for this instance of Debezium Server")
    private boolean enabled = false;

    @JsonProperty(defaultValue = "8080")
    @JsonPropertyDescription("Port number used by the k8s service exposing the API")
    private int port = 8080;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public ConfigMapping<DebeziumServer> asConfiguration(DebeziumServer primary) {
        return ConfigMapping.empty(primary)
                .put("enabled", enabled);
    }
}
