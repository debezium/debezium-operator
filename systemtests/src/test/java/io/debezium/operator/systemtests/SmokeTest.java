/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.systemtests;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.systemtests.resources.NamespaceHolder;
import io.debezium.operator.systemtests.resources.operator.DebeziumOperatorBundleResource;
import io.debezium.operator.systemtests.resources.server.DebeziumServerGenerator;
import io.skodjob.testframe.resources.KubeResourceManager;

public class SmokeTest extends TestBase {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    void firstInstanceIT() {
        String namespace = NamespaceHolder.INSTANCE.getCurrentNamespace();
        DebeziumOperatorBundleResource operatorBundleResource = new DebeziumOperatorBundleResource();
        operatorBundleResource.configureAsDefault(namespace);
        logger.info("Deploying Operator");
        operatorBundleResource.deploy();
        logger.info("Deploying Debezium Server");
        DebeziumServer server = DebeziumServerGenerator.generateDefaultMysqlToRedis(namespace);
        KubeResourceManager.getInstance().createResourceWithWait(server);
        assertStreamingWorks();
    }

    @Test
    void secondInstanceIT() {
        String namespace = NamespaceHolder.INSTANCE.getCurrentNamespace();
        DebeziumOperatorBundleResource operatorBundleResource = new DebeziumOperatorBundleResource();
        operatorBundleResource.configureAsDefault(namespace);
        logger.info("Deploying Operator");
        operatorBundleResource.deploy();
        logger.info("Deploying Debezium Server");
        DebeziumServer server = DebeziumServerGenerator.generateDefaultMysqlToRedis(namespace);
        KubeResourceManager.getInstance().createResourceWithWait(server);
        assertStreamingWorks();
    }
}
