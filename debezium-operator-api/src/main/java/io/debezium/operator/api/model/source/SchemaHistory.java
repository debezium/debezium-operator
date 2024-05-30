/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.source;

import java.util.Objects;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.debezium.operator.api.config.ConfigMappable;
import io.debezium.operator.api.config.ConfigMapping;
import io.debezium.operator.api.model.source.storage.CustomStore;
import io.debezium.operator.api.model.source.storage.Store;
import io.debezium.operator.api.model.source.storage.schema.FileSchemaHistoryStore;
import io.debezium.operator.api.model.source.storage.schema.InMemorySchemaHistoryStore;
import io.debezium.operator.api.model.source.storage.schema.RedisSchemaHistoryStore;
import io.debezium.operator.docs.annotations.Documented;
import io.sundr.builder.annotations.Buildable;

@Documented
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder", lazyCollectionInitEnabled = false)
public class SchemaHistory implements ConfigMappable {

    @JsonPropertyDescription("File backed schema history store configuration")
    private FileSchemaHistoryStore file;
    @JsonPropertyDescription("Memory backed schema history store configuration")
    private InMemorySchemaHistoryStore memory;
    @JsonPropertyDescription("Redis backed schema history store configuration")
    private RedisSchemaHistoryStore redis;
    @JsonPropertyDescription("Arbitrary schema history store configuration")
    private CustomStore store;

    public FileSchemaHistoryStore getFile() {
        return file;
    }

    public void setFile(FileSchemaHistoryStore file) {
        this.file = file;
    }

    public InMemorySchemaHistoryStore getMemory() {
        return memory;
    }

    public void setMemory(InMemorySchemaHistoryStore memory) {
        this.memory = memory;
    }

    public RedisSchemaHistoryStore getRedis() {
        return redis;
    }

    public void setRedis(RedisSchemaHistoryStore redis) {
        this.redis = redis;
    }

    public CustomStore getStore() {
        return store;
    }

    public void setStore(CustomStore store) {
        this.store = store;
    }

    @JsonIgnore
    public Store getActiveStore() {
        return Stream.of(file, memory, redis, store)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(InMemorySchemaHistoryStore::new);
    }

    @Override
    public ConfigMapping asConfiguration() {
        return getActiveStore().asConfiguration();
    }
}
