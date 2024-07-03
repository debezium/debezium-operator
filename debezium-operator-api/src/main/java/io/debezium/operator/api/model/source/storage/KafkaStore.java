/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.source.storage;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.debezium.operator.api.config.ConfigMapping;

public class KafkaStore extends AbstractStore {

    @JsonPropertyDescription("A list of host/port pairs that the connector uses for establishing an initial connection to the Kafka cluster")
    private String bootstrapServers;
    @JsonPropertyDescription("The name of the Kafka topic where offsets are to be stored")
    private String topic;
    @JsonPropertyDescription("The number of partitions used when creating the offset storage topic")
    private int partitions;
    @JsonPropertyDescription("Replication factor used when creating the offset storage topic")
    private int replicationFactor;

    public KafkaStore(String type) {
        super(type);
    }

    public KafkaStore(String configPrefix, String type) {
        super(configPrefix, type);
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getPartitions() {
        return partitions;
    }

    public void setPartitions(int partitions) {
        this.partitions = partitions;
    }

    public int getReplicationFactor() {
        return replicationFactor;
    }

    public void setReplicationFactor(int replicationFactor) {
        this.replicationFactor = replicationFactor;
    }

    @Override
    public ConfigMapping typeConfiguration() {
        return ConfigMapping.empty()
                .put("topic", topic)
                .put("partitions", partitions)
                .put("replication.factor", replicationFactor)
                .putAll(kafkaProps());
    }

    protected ConfigMapping kafkaProps() {
        return ConfigMapping.empty().put("bootstrap.servers", bootstrapServers);
    }
}
