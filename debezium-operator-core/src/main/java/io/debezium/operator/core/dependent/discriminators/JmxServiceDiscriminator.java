/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core.dependent.discriminators;

import static io.debezium.operator.api.model.CommonLabels.hasLabel;

import java.util.Optional;

import io.debezium.operator.api.model.CommonLabels;
import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.core.dependent.JmxServiceDependent;
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
}
