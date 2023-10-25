/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.dependent.discriminators;

import java.util.Optional;

import io.debezium.operator.DebeziumServer;
import io.debezium.operator.dependent.JmxServiceDependent;
import io.debezium.operator.model.CommonLabels;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Service;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ResourceDiscriminator;

public class JmxServiceDiscriminator implements ResourceDiscriminator<Service, DebeziumServer> {

    @Override
    public Optional<Service> distinguish(Class<Service> resource, DebeziumServer primary, Context<DebeziumServer> context) {
        return context.getSecondaryResourcesAsStream(Service.class)
                .filter(s -> hasLabel(s, CommonLabels.KEY_DBZ_CLASSIFIER, JmxServiceDependent.JMX_CLASSIFIER))
                .findFirst();
    }

    private boolean hasLabel(HasMetadata resource, String key, String expectedValue) {
        var labels = resource.getMetadata().getLabels();
        var actualValue = labels.get(key);

        return actualValue != null && actualValue.equals(expectedValue);
    }
}
