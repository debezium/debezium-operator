/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.api.model.DebeziumServerBuilder;
import io.debezium.operator.api.model.DebeziumServerSpecBuilder;
import io.debezium.operator.api.model.source.OffsetBuilder;
import io.debezium.operator.api.model.source.SchemaHistoryBuilder;
import io.debezium.operator.api.model.source.SourceBuilder;
import io.debezium.operator.api.model.source.storage.offset.JdbcOffsetStoreBuilder;
import io.debezium.operator.api.model.source.storage.offset.JdbcOffsetTableConfig;
import io.debezium.operator.api.model.source.storage.offset.JdbcOffsetTableConfigBuilder;
import io.debezium.operator.api.model.source.storage.schema.JdbcSchemaHistoryStoreBuilder;
import io.debezium.operator.api.model.source.storage.schema.JdbcSchemaHistoryTableConfig;
import io.debezium.operator.api.model.source.storage.schema.JdbcSchemaHistoryTableConfigBuilder;

public class JdbcOffsetConfigTest {

    static DebeziumServer server;

    public static final String OFFSET_CLASS = "io.debezium.storage.jdbc.offset.JdbcOffsetBackingStore";
    public static final String OFFSET_TABLE_NAME = "offsets";

    public static final String SCHEMA_CLASS = "io.debezium.storage.jdbc.history.JdbcSchemaHistory";
    public static final String SCHEMA_TABLE_NAME = "schemas";

    public static final String USER = "tom";
    public static final String PASSWORD = "secret";
    public static final String URL = "jdbc:postgresql://localhost:5432/debezium";
    public static final int MAX_RETRIES = 3;
    public static final long RETRY_DELAY_MS = 1000;
    public static final String INVALID_FAKE_SQL = "fake";

    @BeforeAll
    static void setup() {
        server = new DebeziumServerBuilder()
                .withSpec(new DebeziumServerSpecBuilder()
                        .withSource(new SourceBuilder()
                                .withOffset(new OffsetBuilder()
                                        .withJdbc(new JdbcOffsetStoreBuilder()
                                                .withUrl(URL)
                                                .withUser(USER)
                                                .withPassword(PASSWORD)
                                                .withMaxRetries(MAX_RETRIES)
                                                .withRetryDelay(RETRY_DELAY_MS)
                                                .withTable(new JdbcOffsetTableConfigBuilder()
                                                        .withName(OFFSET_TABLE_NAME)
                                                        .withDdl(INVALID_FAKE_SQL)
                                                        .withInsert(INVALID_FAKE_SQL)
                                                        .withSelect(INVALID_FAKE_SQL)
                                                        .withDelete(INVALID_FAKE_SQL)
                                                        .build())
                                                .build())
                                        .build())
                                .withSchemaHistory(new SchemaHistoryBuilder()
                                        .withJdbc(new JdbcSchemaHistoryStoreBuilder()
                                                .withUrl(URL)
                                                .withUser(USER)
                                                .withPassword(PASSWORD)
                                                .withMaxRetries(MAX_RETRIES)
                                                .withRetryDelay(RETRY_DELAY_MS)
                                                .withTable(new JdbcSchemaHistoryTableConfigBuilder()
                                                        .withName(SCHEMA_TABLE_NAME)
                                                        .withDdl(INVALID_FAKE_SQL)
                                                        .withInsert(INVALID_FAKE_SQL)
                                                        .withSelect(INVALID_FAKE_SQL)
                                                        .withDataExistsSelect(INVALID_FAKE_SQL)
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();
    }

    @Test
    public void shouldHaveJdbcOffsetConfig() {
        var config = server.asConfiguration().getAsMapSimple();

        assertThat(config)
                .containsEntry("debezium.source.offset.storage", OFFSET_CLASS)
                .containsEntry("debezium.source.offset.storage.jdbc.url", URL)
                .containsEntry("debezium.source.offset.storage.jdbc.user", USER)
                .containsEntry("debezium.source.offset.storage.jdbc.password", PASSWORD)
                .containsEntry("debezium.source.offset.storage.jdbc.retry.max.attempts", String.valueOf(MAX_RETRIES))
                .containsEntry("debezium.source.offset.storage.jdbc.wait.retry.delay.ms", String.valueOf(RETRY_DELAY_MS))
                .containsEntry("debezium.source.offset.storage.jdbc.table.name", OFFSET_TABLE_NAME)
                .containsEntry("debezium.source.offset.storage.jdbc.table.ddl", INVALID_FAKE_SQL)
                .containsEntry("debezium.source.offset.storage.jdbc.table.insert", INVALID_FAKE_SQL)
                .containsEntry("debezium.source.offset.storage.jdbc.table.select", INVALID_FAKE_SQL)
                .containsEntry("debezium.source.offset.storage.jdbc.table.delete", INVALID_FAKE_SQL);
    }

    @Test
    public void shouldHaveDefaultJdbcOffsetTableConfig() {
        var copy = new DebeziumServerBuilder(server).build();
        copy.getSpec().getSource().getOffset().getJdbc().setTable(new JdbcOffsetTableConfig());
        var config = copy.asConfiguration().getAsMapSimple();

        assertThat(config)
                .doesNotContainKey("debezium.source.offset.storage.jdbc.table.name")
                .doesNotContainKey("debezium.source.offset.storage.jdbc.table.ddl")
                .doesNotContainKey("debezium.source.offset.storage.jdbc.table.insert")
                .doesNotContainKey("debezium.source.offset.storage.jdbc.table.select")
                .doesNotContainKey("debezium.source.offset.storage.jdbc.table.delete");
    }

    @Test
    public void shouldHaveJdbcSchemaHistoryConfig() {
        var config = server.asConfiguration().getAsMapSimple();

        assertThat(config)
                .containsEntry("debezium.source.schema.history.internal", SCHEMA_CLASS)
                .containsEntry("debezium.source.schema.history.internal.jdbc.url", URL)
                .containsEntry("debezium.source.schema.history.internal.jdbc.user", USER)
                .containsEntry("debezium.source.schema.history.internal.jdbc.password", PASSWORD)
                .containsEntry("debezium.source.schema.history.internal.jdbc.retry.max.attempts", String.valueOf(MAX_RETRIES))
                .containsEntry("debezium.source.schema.history.internal.jdbc.wait.retry.delay.ms", String.valueOf(RETRY_DELAY_MS))
                .containsEntry("debezium.source.schema.history.internal.jdbc.table.name", SCHEMA_TABLE_NAME)
                .containsEntry("debezium.source.schema.history.internal.jdbc.table.ddl", INVALID_FAKE_SQL)
                .containsEntry("debezium.source.schema.history.internal.jdbc.table.insert", INVALID_FAKE_SQL)
                .containsEntry("debezium.source.schema.history.internal.jdbc.table.select", INVALID_FAKE_SQL)
                .containsEntry("debezium.source.schema.history.internal.jdbc.table.exists", INVALID_FAKE_SQL);
    }

    @Test
    public void shouldHaveDefaultJdbcSchemaHistoryTableConfig() {
        var copy = new DebeziumServerBuilder(server).build();
        copy.getSpec().getSource().getSchemaHistory().getJdbc().setTable(new JdbcSchemaHistoryTableConfig());
        var config = copy.asConfiguration().getAsMapSimple();

        assertThat(config)
                .doesNotContainKey("debezium.source.schema.history.internal.jdbc.table.name")
                .doesNotContainKey("debezium.source.schema.history.internal.jdbc.table.ddl")
                .doesNotContainKey("debezium.source.schema.history.internal.jdbc.table.insert")
                .doesNotContainKey("debezium.source.schema.history.internal.jdbc.table.select")
                .doesNotContainKey("debezium.source.schema.history.internal.jdbc.table.exists");
    }
}
