/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.source.storage.schema;

import io.debezium.operator.api.model.source.storage.KafkaStore;
import io.debezium.operator.docs.annotations.Documented;
import io.sundr.builder.annotations.Buildable;

@Documented
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder", lazyCollectionInitEnabled = false)
public class KafkaSchemaHistoryStore extends KafkaStore {
    public static final String TYPE = "io.debezium.storage.kafka.history.KafkaSchemaHistory";
    public static final String CONFIG_PREFIX = "kafka";

    public KafkaSchemaHistoryStore() {
        super(CONFIG_PREFIX, TYPE);
    }
}
