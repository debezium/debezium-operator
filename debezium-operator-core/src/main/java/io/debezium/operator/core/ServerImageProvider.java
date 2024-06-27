/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.debezium.operator.api.model.DebeziumServer;

@ApplicationScoped
public class ServerImageProvider {

    public static final String DEFAULT_SERVER_IMAGE = "quay.io/debezium/server";
    public static final String DEFAULT_SERVER_TAG_NIGHTLY = "nightly";

    @ConfigProperty(name = "debezium.server.image.name", defaultValue = DEFAULT_SERVER_IMAGE)
    String defaultImageName;

    @ConfigProperty(name = "debezium.server.image.tag")
    Optional<String> defaultExplicitImageTag;

    @ConfigProperty(name = "quarkus.application.version")
    String applicationVersion;

    /**
     * Determines the image to use for the given server
     *
     * @param server server custom resource
     * @return full image name
     */
    public String getImage(DebeziumServer server) {
        var image = server.getSpec().getImage();

        if (image == null) {
            image = defaultImageName + ":" + getImageTag(server);
        }

        return image;
    }

    /**
     * Determines the image tag to use for the given server
     *
     * @param server server custom resource
     * @return image tag
     */
    public String getImageTag(DebeziumServer server) {
        var version = server.getSpec().getVersion();

        return (version != null) ? version : getImageTag();
    }

    private String getImageTag() {
        return defaultExplicitImageTag.orElseGet(this::getImageTagFromAppVersion);
    }

    private String getImageTagFromAppVersion() {
        if (isNightly()) {
            return DEFAULT_SERVER_TAG_NIGHTLY;
        }
        var sep = applicationVersion.lastIndexOf("-");
        var version = applicationVersion.substring(0, sep);
        var classifier = applicationVersion.substring(sep + 1);
        var finalClassifier = classifier.substring(0, 1).toUpperCase() + classifier.substring(1);

        return version + "." + finalClassifier;
    }

    private boolean isNightly() {
        return applicationVersion.endsWith("nightly");
    }
}
