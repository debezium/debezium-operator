/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.debezium.operator.docs.annotations.Documented;

@JsonPropertyOrder({ "enabled", "port", "auth" })
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Documented
public class JmxConfig {

    @JsonPropertyDescription("Whether JMX should be enabled for this Debezium Server instance.")
    @JsonProperty(defaultValue = "false")
    private boolean enabled = false;
    @JsonPropertyDescription("JMX port.")
    @JsonProperty(defaultValue = "1099")
    private int port = 1099;
    @JsonPropertyDescription("JMX authentication config.")
    private JmxAuthentication authentication;

    public JmxConfig() {
        this.authentication = new JmxAuthentication();
    }

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

    public JmxAuthentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(JmxAuthentication authentication) {
        this.authentication = authentication;
    }
}
