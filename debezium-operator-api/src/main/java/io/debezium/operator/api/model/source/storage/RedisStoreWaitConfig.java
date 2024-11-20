/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.source.storage;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.debezium.operator.api.config.ConfigMappable;
import io.debezium.operator.api.config.ConfigMapping;
import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.docs.annotations.Documented;
import io.sundr.builder.annotations.Buildable;

@Documented
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder", lazyCollectionInitEnabled = false)
public class RedisStoreWaitConfig implements ConfigMappable<DebeziumServer> {

    @JsonPropertyDescription("In case of Redis with replica, this allows to verify that the data has been written to replica")
    @JsonProperty(defaultValue = "false")
    private boolean enabled = false;

    @JsonPropertyDescription("Timeout in ms when waiting for replica")
    @JsonProperty(defaultValue = "1000")
    private long timeoutMs = 1000L;

    @JsonPropertyDescription("Enables retry on wait for replica")
    @JsonProperty(defaultValue = "false")
    private boolean retry = false;

    @JsonPropertyDescription("Delay of retry on wait")
    @JsonProperty(defaultValue = "1000")
    private long retryDelayMs = 1000L;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public boolean isRetry() {
        return retry;
    }

    public void setRetry(boolean retry) {
        this.retry = retry;
    }

    public long getRetryDelayMs() {
        return retryDelayMs;
    }

    public void setRetryDelayMs(long retryDelayMs) {
        this.retryDelayMs = retryDelayMs;
    }

    @Override
    public ConfigMapping<DebeziumServer> asConfiguration(DebeziumServer primary) {
        return ConfigMapping.empty(primary)
                .put("enabled", enabled)
                .put("timeout.ms", timeoutMs)
                .put("retry.enabled", retry)
                .put("retry.delay.ms", retryDelayMs);
    }
}
