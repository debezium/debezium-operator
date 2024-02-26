/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.systemtests.resources.server;

import io.debezium.operator.api.model.DebeziumServer;
import io.fabric8.kubernetes.api.model.DefaultKubernetesResourceList;

import lombok.ToString;

@ToString(callSuper = true)
public class DebeziumServerList extends DefaultKubernetesResourceList<DebeziumServer> {
    private static final long serialVersionUID = 1L;
}
