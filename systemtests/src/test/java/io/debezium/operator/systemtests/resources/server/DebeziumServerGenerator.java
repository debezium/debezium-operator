/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.systemtests.resources.server;

import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.systemtests.ResourceUtils;

public class DebeziumServerGenerator {
    public static DebeziumServer generateDefaultMysqlToRedis(String namespace) {
        DebeziumServer server = ResourceUtils.readYaml(DebeziumServerGenerator.class.getClassLoader().getResource("server/default-server.yaml"), DebeziumServer.class);
        server.getMetadata().setNamespace(namespace);
        return server;
    }
}
