/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core;

import java.util.Objects;

import io.debezium.operator.api.model.DebeziumServer;
import io.javaoperatorsdk.operator.processing.event.source.filter.OnUpdateFilter;

/**
 * Filters {@link DebeziumServer} update events reaching the reconciler.
 * <p>
 * The controller disables {@code generationAwareEventProcessing} so that changes to the
 * {@code debezium.io/stop} annotation (which live in {@code metadata} and therefore never bump
 * {@code metadata.generation}) can trigger reconciliation. To retain the benefits of
 * generation-aware processing, this filter accepts an update only when the generation changed
 * (i.e. a real spec change) or when the stop flag was toggled. In particular, it rejects the
 * operator's own status patches, preventing reconciliation loops.
 */
public class DebeziumServerUpdateFilter implements OnUpdateFilter<DebeziumServer> {

    @Override
    public boolean accept(DebeziumServer newResource, DebeziumServer oldResource) {
        return generationChanged(newResource, oldResource)
                || newResource.isStopped() != oldResource.isStopped();
    }

    private static boolean generationChanged(DebeziumServer newResource, DebeziumServer oldResource) {
        return !Objects.equals(newResource.getMetadata().getGeneration(), oldResource.getMetadata().getGeneration());
    }
}
