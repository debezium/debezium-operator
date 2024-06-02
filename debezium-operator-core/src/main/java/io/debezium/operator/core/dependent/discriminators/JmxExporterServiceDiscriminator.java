/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core.dependent.discriminators;

import java.util.Optional;

import io.debezium.operator.api.model.CommonLabels;
import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.core.dependent.JmxExporterServiceDependent;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Service;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ResourceDiscriminator;

public class JmxExporterServiceDiscriminator implements ResourceDiscriminator<Service, DebeziumServer> {

    @Override
    public Optional<Service> distinguish(Class<Service> resource, DebeziumServer primary, Context<DebeziumServer> context) {
        return context.getSecondaryResourcesAsStream(Service.class)
                .filter(s -> hasLabel(s, CommonLabels.KEY_DBZ_CLASSIFIER, JmxExporterServiceDependent.JMX_EXPORTER_CLASSIFIER))
                .findFirst();
    }

    private boolean hasLabel(HasMetadata resource, String key, String expectedValue) {
        var labels = resource.getMetadata().getLabels();
        var actualValue = labels.get(key);

        return actualValue != null && actualValue.equals(expectedValue);
    }
}
