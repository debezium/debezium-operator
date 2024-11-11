/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core.dependent;

import io.debezium.operator.api.model.DebeziumServer;

/**
 * Describes a dependent resource name as managed by the operator itself when not provided in CRD
 *
 * The {@link ConfigMapDependent} will detect implementors and using the {@code configurationName()} and
 * {@code managedName()} will add the necessary configuration in the relative config.
 */
public interface AutoNamed {

    String configurationName();

    String managedName(DebeziumServer primary);
}
