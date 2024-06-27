/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.debezium.operator.api.model.DebeziumServer;

public class ServerImageProviderTest {
    DebeziumServer debeziumServer = new DebeziumServer();
    ServerImageProvider serverImageProvider = new ServerImageProvider();

    @BeforeEach
    void setUp() {
        serverImageProvider.defaultImageName = "quay.io/debezium/server";
        serverImageProvider.defaultExplicitImageTag = Optional.empty();
        serverImageProvider.applicationVersion = "2.5.0-final";
    }

    @Test
    void shouldReturnImageTag() {
        debeziumServer.getSpec().setVersion("latest");

        assertThat(serverImageProvider.getImageTag(debeziumServer)).isEqualTo("latest");
    }

    @Test
    void shouldReturnDefaultTag() {
        serverImageProvider.defaultExplicitImageTag = Optional.of("custom");

        assertThat(serverImageProvider.getImageTag(debeziumServer)).isEqualTo("custom");
    }

    @Test
    void shouldReturnTagFromApplicationVersion() {
        assertThat(serverImageProvider.getImageTag(debeziumServer)).isEqualTo("2.5.0.Final");
    }

    @Test
    void shouldReturnNightlyTagFromApplicationVersion() {
        serverImageProvider.applicationVersion = "2.5.0-nightly";
        assertThat(serverImageProvider.getImageTag(debeziumServer)).isEqualTo("nightly");
    }
}
