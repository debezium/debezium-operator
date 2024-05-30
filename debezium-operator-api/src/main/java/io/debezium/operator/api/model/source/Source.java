/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.source;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.debezium.operator.api.config.ConfigMappable;
import io.debezium.operator.api.config.ConfigMapping;
import io.debezium.operator.api.model.ConfigProperties;
import io.debezium.operator.docs.annotations.Documented;
import io.sundr.builder.annotations.Buildable;

@Documented
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder", lazyCollectionInitEnabled = false)
public class Source implements ConfigMappable {

    @JsonPropertyDescription("Fully qualified name of source connector Java class.")
    @JsonProperty(value = "class", required = true)
    private String sourceClass;

    @JsonPropertyDescription("Offset store configuration")
    private Offset offset = new Offset();

    @JsonPropertyDescription("Schema history store configuration")
    private SchemaHistory schemaHistory = new SchemaHistory();

    @JsonPropertyDescription("Source connector configuration properties.")
    private ConfigProperties config = new ConfigProperties();

    public String getSourceClass() {
        return sourceClass;
    }

    public void setSourceClass(String clazz) {
        this.sourceClass = clazz;
    }

    public Offset getOffset() {
        return offset;
    }

    public void setOffset(Offset offset) {
        this.offset = offset;
    }

    public SchemaHistory getSchemaHistory() {
        return schemaHistory;
    }

    public void setSchemaHistory(SchemaHistory schemaHistory) {
        this.schemaHistory = schemaHistory;
    }

    public ConfigProperties getConfig() {
        return config;
    }

    public void setConfig(ConfigProperties config) {
        this.config = config;
    }

    @Override
    public ConfigMapping asConfiguration() {
        return ConfigMapping.empty()
                .put("connector.class", sourceClass)
                .putAll(this.config)
                .putAll("offset", offset)
                .putAll("schema.history.internal", schemaHistory);
    }
}
