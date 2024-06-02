/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.runtime.metrics;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.debezium.operator.docs.annotations.Documented;
import io.sundr.builder.annotations.Buildable;

@Documented
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder", lazyCollectionInitEnabled = false)
public class Metrics {

    @JsonPropertyDescription("Prometheus JMX exporter configuration")
    private JmxExporter jmxExporter = new JmxExporter();

    public JmxExporter getJmxExporter() {
        return jmxExporter;
    }

    public void setJmxExporter(JmxExporter jmxExporter) {
        this.jmxExporter = jmxExporter;
    }
}
