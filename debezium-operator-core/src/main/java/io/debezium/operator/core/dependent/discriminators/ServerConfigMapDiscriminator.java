/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core.dependent.discriminators;

import static io.debezium.operator.api.model.CommonLabels.hasLabel;
import static io.debezium.operator.core.dependent.ConfigMapDependent.SERVER_CONFIG_CONFIG_MAP_CLASSIFIER;

import java.util.Optional;

import io.debezium.operator.api.model.CommonLabels;
import io.debezium.operator.api.model.DebeziumServer;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ResourceDiscriminator;

public class ServerConfigMapDiscriminator implements ResourceDiscriminator<ConfigMap, DebeziumServer> {

    @Override
    public Optional<ConfigMap> distinguish(Class<ConfigMap> resource, DebeziumServer primary, Context<DebeziumServer> context) {
        return context.getSecondaryResourcesAsStream(ConfigMap.class)
                .filter(s -> hasLabel(s, CommonLabels.KEY_DBZ_CLASSIFIER, SERVER_CONFIG_CONFIG_MAP_CLASSIFIER))
                .findFirst();
    }
}
