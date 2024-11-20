/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.source.storage.offset;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.debezium.operator.api.config.ConfigMapping;
import io.debezium.operator.api.model.ConfigProperties;
import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.api.model.source.storage.KafkaStore;
import io.debezium.operator.docs.annotations.Documented;
import io.sundr.builder.annotations.Buildable;

@Documented
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder", lazyCollectionInitEnabled = false)
public class KafkaOffsetStore extends KafkaStore {
    public static final String TYPE = "org.apache.kafka.connect.storage.KafkaOffsetBackingStore";

    @JsonPropertyDescription("Additional Kafka client properties.")
    private ConfigProperties props = new ConfigProperties();

    public ConfigProperties getProps() {
        return props;
    }

    public void setProps(ConfigProperties props) {
        this.props = props;
    }

    public KafkaOffsetStore() {
        super(TYPE);
    }

    @Override
    protected ConfigMapping<DebeziumServer> kafkaProps(DebeziumServer primary) {
        return ConfigMapping.prefixed(primary, "debezium.source")
                .put("bootstrap.servers", getBootstrapServers())
                .putAll(props)
                .asAbsolute();
    }
}
