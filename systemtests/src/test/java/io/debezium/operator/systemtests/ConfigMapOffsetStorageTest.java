/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.systemtests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.api.model.source.Offset;
import io.debezium.operator.api.model.source.OffsetBuilder;
import io.debezium.operator.systemtests.resources.NamespaceHolder;
import io.debezium.operator.systemtests.resources.operator.DebeziumOperatorBundleResource;
import io.debezium.operator.systemtests.resources.server.DebeziumServerGenerator;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.skodjob.testframe.resources.KubeResourceManager;

public class ConfigMapOffsetStorageTest extends TestBase {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    void testConfigMapOffsetStorage() {

        String namespace = NamespaceHolder.INSTANCE.getCurrentNamespace();
        DebeziumOperatorBundleResource operatorBundleResource = new DebeziumOperatorBundleResource();
        operatorBundleResource.configureAsDefault(namespace);
        logger.info("Deploying Operator");
        operatorBundleResource.deploy();
        logger.info("Deploying Debezium Server");
        DebeziumServer server = DebeziumServerGenerator.generateDefaultMysqlToRedis(namespace);

        Offset offset = new OffsetBuilder()
                .withNewConfigMap()
                .endConfigMap()
                .build();
        server.getSpec().getSource().setOffset(offset);

        KubeResourceManager.get().createResourceWithWait(server);
        assertStreamingWorks();

        ConfigMap configMap = KubeResourceManager.get().kubeClient().getClient()
                .configMaps()
                .inNamespace(namespace)
                .withName("my-debezium-offsets")
                .get();

        assertThat(configMap.getBinaryData()).containsOnlyKeys("redis.server-inventory");

        String offsets = new String(Base64.getDecoder().decode(configMap.getBinaryData().get("redis.server-inventory")));
        assertThat(offsets).contains("pos", "file");
    }

    @Test
    void configMapOffsetStorageMustNotBeCreatedIfAlreadyExists() {

        String namespace = NamespaceHolder.INSTANCE.getCurrentNamespace();
        DebeziumOperatorBundleResource operatorBundleResource = new DebeziumOperatorBundleResource();
        operatorBundleResource.configureAsDefault(namespace);
        logger.info("Deploying Operator");
        operatorBundleResource.deploy();

        KubeResourceManager.get().createResourceWithWait(new ConfigMapBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withNamespace(namespace)
                        .withName("debezium-offsets")
                        .build())
                .build());

        logger.info("Deploying Debezium Server");
        DebeziumServer server = DebeziumServerGenerator.generateDefaultMysqlToRedis(namespace);

        Offset offset = new OffsetBuilder()
                .withNewConfigMap()
                .withName("debezium-offsets")
                .endConfigMap()
                .build();
        server.getSpec().getSource().setOffset(offset);

        KubeResourceManager.get().createResourceWithWait(server);
        assertStreamingWorks();

        ConfigMap configMap = KubeResourceManager.get().kubeClient().getClient()
                .configMaps()
                .inNamespace(namespace)
                .withName("debezium-offsets")
                .get();

        assertThat(configMap.getMetadata().getLabels()).doesNotContainKey("debezium.io/component");
        assertThat(configMap.getBinaryData()).containsOnlyKeys("redis.server-inventory");

        String offsets = new String(Base64.getDecoder().decode(configMap.getBinaryData().get("redis.server-inventory")));
        assertThat(offsets).contains("pos", "file");
    }
}
