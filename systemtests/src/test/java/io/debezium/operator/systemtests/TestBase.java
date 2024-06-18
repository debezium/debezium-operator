/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.systemtests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.debezium.operator.systemtests.resources.NamespaceHolder;
import io.debezium.operator.systemtests.resources.annotations.DebeziumResourceTypes;
import io.debezium.operator.systemtests.resources.databases.MysqlResource;
import io.debezium.operator.systemtests.resources.dmt.DmtClient;
import io.debezium.operator.systemtests.resources.dmt.DmtResource;
import io.debezium.operator.systemtests.resources.sinks.RedisResource;
import io.fabric8.kubernetes.client.LocalPortForward;
import io.skodjob.testframe.annotations.ResourceManager;
import io.skodjob.testframe.annotations.TestVisualSeparator;

import okhttp3.Response;

@ResourceManager
@TestVisualSeparator
@DebeziumResourceTypes
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestBase {
    private static final Logger logger = LoggerFactory.getLogger(TestBase.class);
    private final DmtResource dmtResource = new DmtResource();
    private final String portForwardHost = "127.0.0.1";
    private int portForwardPort = 8080;

    @BeforeAll
    void initDefault() {
        String namespace = NamespaceHolder.INSTANCE.getCurrentNamespace();
        MysqlResource dts = new MysqlResource();
        dts.configureAsDefault(namespace);
        logger.info("Deploying MySQL");
        dts.deploy();
        RedisResource redis = new RedisResource();
        redis.configureAsDefault(namespace);
        logger.info("Deploying Redis");
        redis.deploy();

        logger.info("Deploying DMT");
        dmtResource.configureAsDefault(namespace);
        dmtResource.deploy();
        NamespaceHolder.INSTANCE.setNamespacedDmt(dmtResource);
    }

    @AfterEach
    void cleanUp() {
        String namespace = NamespaceHolder.INSTANCE.getCurrentNamespace();
        try (LocalPortForward lcp = dmtResource.portForward(portForwardPort, namespace)) {
            DmtClient.waitForDmt(portForwardHost, portForwardPort, Duration.ofSeconds(5));
            Response redis = DmtClient.resetRedis(portForwardHost, portForwardPort);
            assertThat(redis.code()).isEqualTo(200);
            redis.close();
            Response mysql = DmtClient.resetMysql(portForwardHost, portForwardPort);
            assertThat(mysql.code()).isEqualTo(200);
            mysql.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void assertStreamingWorks() {
        String namespace = NamespaceHolder.INSTANCE.getCurrentNamespace();
        try (LocalPortForward lcp = dmtResource.portForward(8080, namespace)) {
            DmtClient.waitForDmt(portForwardHost, portForwardPort, Duration.ofSeconds(5));
            DmtClient.insertTestDataToDatabase(portForwardHost, portForwardPort, 10);
            DmtClient.waitForFilledRedis(portForwardHost, portForwardPort, Duration.ofSeconds(40), "inventory.inventory.operator_test");
            await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofMillis(500))
                    .until(() -> DmtClient.digStreamedData(portForwardHost, portForwardPort, 10) == 10);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
