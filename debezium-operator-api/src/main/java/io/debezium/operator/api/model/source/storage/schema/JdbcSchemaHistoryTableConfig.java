/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.source.storage.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.debezium.operator.api.config.ConfigMappable;
import io.debezium.operator.api.config.ConfigMapping;
import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.docs.annotations.Documented;
import io.sundr.builder.annotations.Buildable;

@Documented
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder", lazyCollectionInitEnabled = false)
public class JdbcSchemaHistoryTableConfig implements ConfigMappable<DebeziumServer> {

    @JsonPropertyDescription("The name of the offset table")
    @JsonProperty(required = false)
    private String name;

    @JsonPropertyDescription("DDL statement to create the schema history table")
    @JsonProperty(required = false)
    private String ddl;

    @JsonPropertyDescription("Statement used to select from the schema history table")
    @JsonProperty(required = false)
    private String select;

    @JsonPropertyDescription("Statement used to insert into the schema history table")
    @JsonProperty(required = false)
    private String insert;

    @JsonPropertyDescription("Statement used to check existence of some data in the schema history table")
    @JsonProperty(required = false)
    private String dataExistsSelect;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDdl() {
        return ddl;
    }

    public void setDdl(String ddl) {
        this.ddl = ddl;
    }

    public String getSelect() {
        return select;
    }

    public void setSelect(String select) {
        this.select = select;
    }

    public String getInsert() {
        return insert;
    }

    public void setInsert(String insert) {
        this.insert = insert;
    }

    public String getDataExistsSelect() {
        return dataExistsSelect;
    }

    public void setDataExistsSelect(String dataExistsSelect) {
        this.dataExistsSelect = dataExistsSelect;
    }

    @Override
    public ConfigMapping<DebeziumServer> asConfiguration(DebeziumServer primary) {
        return ConfigMapping.empty(primary)
                .put("name", name)
                .put("ddl", ddl)
                .put("select", select)
                .put("insert", insert)
                .put("exists", dataExistsSelect);
    }
}
