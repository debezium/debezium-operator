/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.source.storage;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.debezium.operator.api.config.ConfigMapping;

public class RedisStore extends AbstractStore {

    @JsonPropertyDescription("Redis host:port used to connect")
    @JsonProperty(required = true)
    private String address;
    @JsonPropertyDescription("Redis username")
    @JsonProperty(required = false)
    private String user;
    @JsonPropertyDescription("Redis password")
    @JsonProperty(required = false)
    private String password;
    @JsonPropertyDescription("Redis username")
    @JsonProperty(defaultValue = "false")
    private boolean sslEnabled = false;
    @JsonPropertyDescription("Redis hash key")
    @JsonProperty(required = false)
    private String key;
    @JsonPropertyDescription("Configures verification of replica writes")
    private RedisStoreWaitConfig wait = new RedisStoreWaitConfig();

    public RedisStore(String type) {
        super(type);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public RedisStoreWaitConfig getWait() {
        return wait;
    }

    public void setWait(RedisStoreWaitConfig wait) {
        this.wait = wait;
    }

    @Override
    public ConfigMapping asConfiguration() {
        return super.asConfiguration()
                .put("address", address)
                .put("user", user)
                .put("password", password)
                .put("ssl.enabled", sslEnabled)
                .put("key", key)
                .putAll("wait", wait);
    }
}
