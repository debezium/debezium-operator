/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.systemtests;

import static io.debezium.operator.systemtests.ConfigProperties.HTTP_POLL_INTERVAL;
import static io.debezium.operator.systemtests.ConfigProperties.HTTP_POLL_TIMEOUT;
import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.AfterAll;
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
import io.debezium.operator.systemtests.resources.logs.MustGatherImpl;
import io.debezium.operator.systemtests.resources.sinks.RedisResource;
import io.fabric8.kubernetes.client.LocalPortForward;
import io.skodjob.testframe.annotations.MustGather;
import io.skodjob.testframe.annotations.ResourceManager;
import io.skodjob.testframe.annotations.TestVisualSeparator;

@ResourceManager(asyncDeletion = false)
@MustGather(config = MustGatherImpl.class)
@TestVisualSeparator
@DebeziumResourceTypes
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestBase {
    private static final Logger logger = LoggerFactory.getLogger(TestBase.class);
    protected final DmtResource dmtResource = new DmtResource();
    protected final String portForwardHost = "127.0.0.1";
    protected int portForwardPort = 8080;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
    private static final String USER_PATH = System.getProperty("user.dir");
    public static final Path LOG_DIR = Paths.get(USER_PATH, "target", "logs")
            .resolve("test-run-" + DATE_FORMAT.format(LocalDateTime.now()));

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
            logger.info("Waiting for DMT available");
            DmtClient.waitForDmt(portForwardHost, portForwardPort, Duration.ofSeconds(HTTP_POLL_TIMEOUT));
            logger.info("Reseting Redis");
            DmtClient.resetRedis(portForwardHost, portForwardPort);
            logger.info("Resetting MySQL");
            DmtClient.resetMysql(portForwardHost, portForwardPort);
        }
        catch (IOException e) {
            logger.error("Error cleaning up Redis and MySQL", e);
            throw new RuntimeException(e);
        }
    }

    public void assertStreamingWorks() {
        assertStreamingWorks(10, 10);
    }

    public void assertStreamingWorks(int messagesToDatabase, int expectedMessages) {
        String namespace = NamespaceHolder.INSTANCE.getCurrentNamespace();
        try (LocalPortForward lcp = dmtResource.portForward(8080, namespace)) {
            DmtClient.waitForDmt(portForwardHost, portForwardPort, Duration.ofSeconds(HTTP_POLL_TIMEOUT));
            DmtClient.insertTestDataToDatabase(portForwardHost, portForwardPort, messagesToDatabase);
            DmtClient.waitForFilledRedis(portForwardHost, portForwardPort, Duration.ofSeconds(60), "inventory.inventory.operator_test");
            await().atMost(Duration.ofMinutes(HTTP_POLL_TIMEOUT))
                    .pollInterval(Duration.ofMillis(HTTP_POLL_INTERVAL))
                    .until(() -> {
                        try {
                            return DmtClient.digStreamedData(portForwardHost, portForwardPort, expectedMessages) == expectedMessages;
                        }
                        catch (ConditionTimeoutException ex) {
                            logger.error(ex.getMessage());
                            return false;
                        }
                    });
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    void resetNamespace() {
        NamespaceHolder.INSTANCE.resetNamespace();
    }

}
