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
public class OtelCollector {

    @JsonPropertyDescription("OTLP collector endpoint")
    private String endpoint;

    @JsonPropertyDescription("JMX metrics scrape interval in milliseconds")
    private int jmxIntervalMs = 10000;

    @JsonPropertyDescription("Metrics export interval in milliseconds")
    private int metricExportIntervalMs = 60000;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public int getJmxIntervalMs() {
        return jmxIntervalMs;
    }

    public void setJmxIntervalMs(int jmxIntervalMs) {
        this.jmxIntervalMs = jmxIntervalMs;
    }

    public int getMetricExportIntervalMs() {
        return metricExportIntervalMs;
    }

    public void setMetricExportIntervalMs(int metricExportIntervalMs) {
        this.metricExportIntervalMs = metricExportIntervalMs;
    }
}
