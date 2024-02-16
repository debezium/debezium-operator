/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.runtime.templates;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.debezium.operator.docs.annotations.Documented;

@JsonPropertyOrder({ "initialDelaySeconds", "periodSeconds", "timeoutSeconds", "failureThreshold" })
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Documented
public class Probe {

    @JsonPropertyDescription("Number of seconds after the container has started before probes are initiated.")
    @JsonProperty(defaultValue = "5")
    private int initialDelaySeconds = 5;
    @JsonPropertyDescription("How often (in seconds) to perform the probe.")
    @JsonProperty(defaultValue = "10")
    private int periodSeconds = 10;
    @JsonPropertyDescription("Number of seconds after which the probe times out.")
    @JsonProperty(defaultValue = "10")
    private int timeoutSeconds = 10;
    @JsonPropertyDescription("Number of failures in a row before the overall check has failed.")
    @JsonProperty(defaultValue = "3")
    private int failureThreshold = 3;

    public int getInitialDelaySeconds() {
        return initialDelaySeconds;
    }

    public void setInitialDelaySeconds(int initialDelaySeconds) {
        this.initialDelaySeconds = initialDelaySeconds;
    }

    public int getPeriodSeconds() {
        return periodSeconds;
    }

    public void setPeriodSeconds(int periodSeconds) {
        this.periodSeconds = periodSeconds;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public int getFailureThreshold() {
        return failureThreshold;
    }

    public void setFailureThreshold(int failureThreshold) {
        this.failureThreshold = failureThreshold;
    }
}
