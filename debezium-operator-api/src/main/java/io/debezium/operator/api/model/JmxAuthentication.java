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

@JsonPropertyOrder({ "enabled", "secret", "accessFile", "secretFile" })
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Documented
public class JmxAuthentication {
    @JsonPropertyDescription("Whether JMX authentication should be enabled for this Debezium Server instance.")
    private boolean enabled = false;
    @JsonPropertyDescription("Secret providing credential files")
    @JsonProperty(required = true)
    private String secret;
    @JsonPropertyDescription("JMX access file name and secret key")
    private String accessFile = "jmxremote.access";
    @JsonPropertyDescription("JMX password file name and secret key")
    private String passwordFile = "jmxremote.password";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getAccessFile() {
        return accessFile;
    }

    public void setAccessFile(String accessFile) {
        this.accessFile = accessFile;
    }

    public String getPasswordFile() {
        return passwordFile;
    }

    public void setPasswordFile(String passwordFile) {
        this.passwordFile = passwordFile;
    }
}
