/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.source;

import java.util.Objects;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.debezium.operator.api.config.ConfigMappable;
import io.debezium.operator.api.config.ConfigMapping;
import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.api.model.source.storage.CustomStore;
import io.debezium.operator.api.model.source.storage.Store;
import io.debezium.operator.api.model.source.storage.offset.ConfigMapOffsetStore;
import io.debezium.operator.api.model.source.storage.offset.FileOffsetStore;
import io.debezium.operator.api.model.source.storage.offset.InMemoryOffsetStore;
import io.debezium.operator.api.model.source.storage.offset.JdbcOffsetStore;
import io.debezium.operator.api.model.source.storage.offset.KafkaOffsetStore;
import io.debezium.operator.api.model.source.storage.offset.RedisOffsetStore;
import io.debezium.operator.docs.annotations.Documented;
import io.sundr.builder.annotations.Buildable;

@Documented
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder", lazyCollectionInitEnabled = false)
public class Offset implements ConfigMappable<DebeziumServer> {

    @JsonPropertyDescription("File backed offset store configuration")
    private FileOffsetStore file;
    @JsonPropertyDescription("Memory backed offset store configuration")
    private InMemoryOffsetStore memory;
    @JsonPropertyDescription("Redis backed offset store configuration")
    private RedisOffsetStore redis;
    @JsonPropertyDescription("Kafka backing store configuration")
    private KafkaOffsetStore kafka;
    @JsonPropertyDescription("JDBC backing store configuration")
    private JdbcOffsetStore jdbc;
    @JsonPropertyDescription("Config map backed offset store configuration")
    private ConfigMapOffsetStore configMap;
    @JsonPropertyDescription("Arbitrary offset store configuration")
    private CustomStore store;
    @JsonPropertyDescription("Interval at which to try commiting offsets")
    @JsonProperty(defaultValue = "60000")
    private long flushMs = 60000L;

    public long getFlushMs() {
        return flushMs;
    }

    public void setFlushMs(long flushMs) {
        this.flushMs = flushMs;
    }

    public FileOffsetStore getFile() {
        return file;
    }

    public void setFile(FileOffsetStore file) {
        this.file = file;
    }

    public InMemoryOffsetStore getMemory() {
        return memory;
    }

    public void setMemory(InMemoryOffsetStore memory) {
        this.memory = memory;
    }

    public RedisOffsetStore getRedis() {
        return redis;
    }

    public void setRedis(RedisOffsetStore redis) {
        this.redis = redis;
    }

    public KafkaOffsetStore getKafka() {
        return kafka;
    }

    public void setKafka(KafkaOffsetStore kafka) {
        this.kafka = kafka;
    }

    public JdbcOffsetStore getJdbc() {
        return jdbc;
    }

    public void setJdbc(JdbcOffsetStore jdbc) {
        this.jdbc = jdbc;
    }

    public ConfigMapOffsetStore getConfigMap() {
        return configMap;
    }

    public void setConfigMap(ConfigMapOffsetStore configmap) {
        this.configMap = configmap;
    }

    public CustomStore getStore() {
        return store;
    }

    public void setStore(CustomStore store) {
        this.store = store;
    }

    @JsonIgnore
    public Store getActiveStore() {
        return Stream.of(file, memory, redis, kafka, jdbc, configMap, store)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(InMemoryOffsetStore::new);
    }

    @Override
    public ConfigMapping<DebeziumServer> asConfiguration(DebeziumServer primary) {
        return ConfigMapping.empty(primary)
                .put("flush.interval.ms", flushMs)
                .putAll("storage", getActiveStore());
    }
}
