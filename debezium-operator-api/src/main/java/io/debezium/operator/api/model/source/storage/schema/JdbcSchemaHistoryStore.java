/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.source.storage.schema;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.debezium.operator.api.config.ConfigMapping;
import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.api.model.source.storage.JdbcStore;
import io.debezium.operator.docs.annotations.Documented;
import io.sundr.builder.annotations.Buildable;

@Documented
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder", lazyCollectionInitEnabled = false)
public class JdbcSchemaHistoryStore extends JdbcStore {

    public static final String TYPE = "io.debezium.storage.jdbc.history.JdbcSchemaHistory";

    @JsonPropertyDescription("The configuration of the offset table")
    private JdbcSchemaHistoryTableConfig table = new JdbcSchemaHistoryTableConfig();

    public JdbcSchemaHistoryStore() {
        super(TYPE);
    }

    public JdbcSchemaHistoryTableConfig getTable() {
        return table;
    }

    public void setTable(JdbcSchemaHistoryTableConfig table) {
        this.table = table;
    }

    @Override
    protected ConfigMapping<DebeziumServer> typeConfiguration(DebeziumServer primary) {
        return super.typeConfiguration(primary)
                .putAll("table", table);
    }
}
