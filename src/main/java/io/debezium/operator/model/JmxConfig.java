/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "enabled", "port" })
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class JmxConfig {

    @JsonPropertyDescription("Whether JMX should be enabled for this Debezium Server instance.")
    boolean enabled = false;

    @JsonPropertyDescription("JMX port.")
    int port = 1099;

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
}
