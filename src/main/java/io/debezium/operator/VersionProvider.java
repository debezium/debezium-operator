/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class VersionProvider {

    public static final String LATEST = "latest";

    @ConfigProperty(name = "debezium.version", defaultValue = LATEST)
    String imageVersion;

    public String getImageVersion() {
        if (isSnapshot()) {
            return LATEST;
        }
        return imageVersion;
    }

    public String getImageVersion(DebeziumServer server) {
        var version = server.getSpec().getVersion();

        return (version != null) ? version : getImageVersion();
    }

    public boolean isSnapshot() {
        return this.imageVersion.endsWith("SNAPSHOT");
    }
}
