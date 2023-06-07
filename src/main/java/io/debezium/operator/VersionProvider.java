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

    private static final String SNAPSHOT = "SNAPSHOT";
    public static final String LATEST = "latest";

    @ConfigProperty(name = "debezium.version", defaultValue = LATEST)
    String imageVersion;

    public String getImageTag() {
        return isSnapshot() ? getRollingTag() : imageVersion;
    }

    public String getImageTag(DebeziumServer server) {
        var version = server.getSpec().getVersion();

        return (version != null) ? version : getImageTag();
    }

    private String getRollingTag() {
        var fullVersion = isSnapshot()
                ? imageVersion.substring(0, imageVersion.length() - SNAPSHOT.length() - 1)
                : imageVersion;

        var parts = fullVersion.split("\\.");
        var major = Integer.parseInt(parts[0]);
        var minor = Integer.parseInt(parts[1]);

        return "%d.%d".formatted(major, minor);
    }

    public boolean isSnapshot() {
        return this.imageVersion.endsWith(SNAPSHOT);
    }
}
