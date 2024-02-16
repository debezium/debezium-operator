/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.runtime.jmx;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.debezium.operator.docs.annotations.Documented;

@JsonPropertyOrder({ "enabled", "secret", "accessFile", "secretFile" })
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Documented
public class JmxAuthentication {

    public static final String JMX_DEFAULT_ACCESS_FILE = "jmxremote.access";
    public static final String JMX_DEFAULT_PASSWORD_FILE = "jmxremote.password";

    @JsonPropertyDescription("Whether JMX authentication should be enabled for this Debezium Server instance.")
    @JsonProperty(defaultValue = "false")
    private boolean enabled = false;
    @JsonPropertyDescription("Secret providing credential files")
    @JsonProperty(required = true)
    private String secret;
    @JsonPropertyDescription("JMX access file name and secret key")
    @JsonProperty(defaultValue = JMX_DEFAULT_ACCESS_FILE)
    private String accessFile = JMX_DEFAULT_ACCESS_FILE;
    @JsonPropertyDescription("JMX password file name and secret key")
    @JsonProperty(defaultValue = JMX_DEFAULT_PASSWORD_FILE)
    private String passwordFile = JMX_DEFAULT_PASSWORD_FILE;

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
