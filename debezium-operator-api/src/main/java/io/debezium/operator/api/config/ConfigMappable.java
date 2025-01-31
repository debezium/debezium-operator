/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.config;

import io.fabric8.kubernetes.api.model.HasMetadata;

public interface ConfigMappable<P extends HasMetadata> {

    ConfigMapping<P> asConfiguration(P primary);
}
