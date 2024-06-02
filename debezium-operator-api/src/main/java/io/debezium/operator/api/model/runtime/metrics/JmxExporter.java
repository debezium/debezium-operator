/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.runtime.metrics;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.debezium.operator.docs.annotations.Documented;
import io.fabric8.kubernetes.api.model.ConfigMapKeySelector;
import io.sundr.builder.annotations.Buildable;

@Documented
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder", lazyCollectionInitEnabled = false)
public class JmxExporter {
    @JsonPropertyDescription("Enables JMX Prometheus exporter")
    private boolean enabled = false;

    @JsonPropertyDescription("Config map key reference which value will be used as configuration file")
    private ConfigMapKeySelector configFrom;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ConfigMapKeySelector getConfigFrom() {
        return configFrom;
    }

    public void setConfigFrom(ConfigMapKeySelector configFrom) {
        this.configFrom = configFrom;
    }
}
