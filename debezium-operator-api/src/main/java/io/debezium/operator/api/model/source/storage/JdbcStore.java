/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.source.storage;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.debezium.operator.api.config.ConfigMapping;
import io.debezium.operator.api.model.DebeziumServer;

public class JdbcStore extends AbstractStore {
    public static final String CONFIG_PREFIX = "jdbc";

    @JsonPropertyDescription("JDBC connection URL")
    private String url;

    @JsonPropertyDescription("Username used to connect to the storage database")
    private String user;

    @JsonPropertyDescription("Password used to connect to the storage database")
    private String password;

    @JsonPropertyDescription("Retry delay on connection failure (in milliseconds)")
    private long retryDelay;

    @JsonPropertyDescription("Maximum number of retries on connection failure")
    private int maxRetries;

    public JdbcStore(String type) {
        super(CONFIG_PREFIX, type);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public long getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    @Override
    protected ConfigMapping<DebeziumServer> typeConfiguration(DebeziumServer primary) {
        return super.typeConfiguration(primary)
                .put("url", url)
                .put("user", user)
                .put("password", password)
                .put("wait.retry.delay.ms", retryDelay)
                .put("retry.max.attempts", maxRetries);
    }
}
