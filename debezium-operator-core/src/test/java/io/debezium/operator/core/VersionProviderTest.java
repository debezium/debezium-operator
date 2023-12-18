/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.debezium.operator.api.model.DebeziumServer;

public class VersionProviderTest {
    DebeziumServer debeziumServer = new DebeziumServer();
    VersionProvider versionProvider = new VersionProvider();

    @Test
    void shouldReturnImageTag() {
        debeziumServer.getSpec().setVersion(VersionProvider.LATEST);

        assertThat(versionProvider.getImageTag(debeziumServer)).isEqualTo(VersionProvider.LATEST);
    }

    @Test
    void shouldReturnRollingTag() {
        versionProvider.imageVersion = "2.5.0-SNAPSHOT";

        assertThat(versionProvider.getImageTag(debeziumServer)).isEqualTo("2.5");
    }

    @Test
    void shouldReturnImageVersion() {
        versionProvider.imageVersion = "2.5.0";

        assertThat(versionProvider.getImageTag(debeziumServer)).isEqualTo("2.5.0");
    }
}
