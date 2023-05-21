/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator;

import io.debezium.operator.config.ConfigMappable;
import io.debezium.operator.config.ConfigMapping;
import io.debezium.operator.model.DebeziumServerSpec;
import io.debezium.operator.model.status.DebeziumServerStatus;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Version("v1alpha1")
@Group("debezium.io")
public class DebeziumServer
        extends CustomResource<DebeziumServerSpec, DebeziumServerStatus>
        implements Namespaced, ConfigMappable {

    @Override
    protected DebeziumServerSpec initSpec() {
        return new DebeziumServerSpec();
    }

    public ConfigMapping asConfiguration() {
        return spec.asConfiguration();
    }
}
