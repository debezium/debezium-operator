/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.systemtests;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.api.model.source.Offset;
import io.debezium.operator.api.model.source.OffsetBuilder;
import io.debezium.operator.systemtests.resources.NamespaceHolder;
import io.debezium.operator.systemtests.resources.dmt.DmtClient;
import io.debezium.operator.systemtests.resources.operator.DebeziumOperatorBundleResource;
import io.debezium.operator.systemtests.resources.server.DebeziumServerGenerator;
import io.debezium.operator.systemtests.resources.sinks.RedisResource;
import io.fabric8.kubernetes.client.LocalPortForward;
import io.skodjob.testframe.resources.KubeResourceManager;

public class RedisOffsetStorageTest extends TestBase {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    void testRedisOffsetStorage() {
        String namespace = NamespaceHolder.INSTANCE.getCurrentNamespace();
        DebeziumOperatorBundleResource operatorBundleResource = new DebeziumOperatorBundleResource();
        operatorBundleResource.configureAsDefault(namespace);
        logger.info("Deploying Operator");
        operatorBundleResource.deploy();
        logger.info("Deploying Debezium Server");
        DebeziumServer server = DebeziumServerGenerator.generateDefaultMysqlToRedis(namespace);

        Offset offset = new OffsetBuilder()
                .withNewRedis()
                .withAddress(RedisResource.getDefaultRedisAddress())
                .endRedis()
                .withFlushMs(10)
                .build();
        server.getSpec().getSource().setOffset(offset);

        KubeResourceManager.getInstance().createResourceWithWait(server);
        assertStreamingWorks();

        try (LocalPortForward lcp = dmtResource.portForward(portForwardPort, namespace)) {
            String redis_offset = DmtClient.readRedisOffsets(portForwardHost, portForwardPort);
            assertThat(redis_offset).contains("file");
            assertThat(redis_offset).contains("pos");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        server.getSpec().getSource().getOffset().getRedis().setKey("metadata:debezium_n:offsets");
        KubeResourceManager.getInstance().createOrUpdateResourceWithWait(server);
        assertStreamingWorks(10, 20);

        try (LocalPortForward lcp = dmtResource.portForward(portForwardPort, namespace)) {
            String redis_offset = DmtClient.readRedisOffsets(portForwardHost, portForwardPort, "metadata:debezium_n:offsets");
            assertThat(redis_offset).contains("file");
            assertThat(redis_offset).contains("pos");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
