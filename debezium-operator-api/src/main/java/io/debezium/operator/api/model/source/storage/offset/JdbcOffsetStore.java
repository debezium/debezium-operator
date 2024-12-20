/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.source.storage.offset;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.debezium.operator.api.config.ConfigMapping;
import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.api.model.source.storage.JdbcStore;
import io.debezium.operator.docs.annotations.Documented;
import io.sundr.builder.annotations.Buildable;

@Documented
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder", lazyCollectionInitEnabled = false)
public class JdbcOffsetStore extends JdbcStore {

    public static final String TYPE = "io.debezium.storage.jdbc.offset.JdbcOffsetBackingStore";

    @JsonPropertyDescription("The configuration of the offset table")
    private JdbcOffsetTableConfig table = new JdbcOffsetTableConfig();

    public JdbcOffsetStore() {
        super(TYPE);
    }

    public JdbcOffsetTableConfig getTable() {
        return table;
    }

    public void setTable(JdbcOffsetTableConfig table) {
        this.table = table;
    }

    @Override
    protected ConfigMapping<DebeziumServer> typeConfiguration(DebeziumServer primary) {
        return super.typeConfiguration(primary)
                .putAll("offset.table", table);
    }
}
