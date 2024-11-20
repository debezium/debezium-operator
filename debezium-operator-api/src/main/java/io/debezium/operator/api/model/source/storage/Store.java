/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.source.storage;

import io.debezium.operator.api.config.ConfigMappable;
import io.debezium.operator.api.model.DebeziumServer;

public interface Store extends ConfigMappable<DebeziumServer> {

    String getType();
}
