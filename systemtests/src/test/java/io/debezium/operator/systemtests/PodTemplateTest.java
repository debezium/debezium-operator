/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.systemtests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.api.model.runtime.templates.PodTemplate;
import io.debezium.operator.systemtests.resources.NamespaceHolder;
import io.debezium.operator.systemtests.resources.operator.DebeziumOperatorBundleResource;
import io.debezium.operator.systemtests.resources.server.DebeziumServerGenerator;
import io.fabric8.kubernetes.api.model.Toleration;
import io.fabric8.kubernetes.api.model.TolerationBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.skodjob.testframe.resources.KubeResourceManager;

public class PodTemplateTest extends TestBase {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    void testNodeSelectorIsAppliedToDeployment() {
        String namespace = NamespaceHolder.INSTANCE.getCurrentNamespace();
        DebeziumOperatorBundleResource operatorBundleResource = new DebeziumOperatorBundleResource();
        operatorBundleResource.configureAsDefault(namespace);
        logger.info("Deploying Operator");
        operatorBundleResource.deploy();

        DebeziumServer server = DebeziumServerGenerator.generateDefaultMysqlToRedis(namespace);
        Map<String, String> nodeSelector = Map.of("kubernetes.io/os", "linux");
        PodTemplate podTemplate = new PodTemplate();
        podTemplate.setNodeSelector(nodeSelector);
        server.getSpec().getRuntime().getTemplates().setPod(podTemplate);

        logger.info("Deploying Debezium Server with nodeSelector");
        KubeResourceManager.getInstance().createResourceWithWait(server);
        assertStreamingWorks();

        Deployment deployment = KubeResourceManager.getKubeClient().getClient()
                .apps().deployments()
                .inNamespace(namespace)
                .withName(server.getMetadata().getName())
                .get();

        assertThat(deployment).isNotNull();
        assertThat(deployment.getSpec().getTemplate().getSpec().getNodeSelector())
                .containsAllEntriesOf(nodeSelector);
    }

    @Test
    void testTolerationsAreAppliedToDeployment() {
        String namespace = NamespaceHolder.INSTANCE.getCurrentNamespace();
        DebeziumOperatorBundleResource operatorBundleResource = new DebeziumOperatorBundleResource();
        operatorBundleResource.configureAsDefault(namespace);
        logger.info("Deploying Operator");
        operatorBundleResource.deploy();

        DebeziumServer server = DebeziumServerGenerator.generateDefaultMysqlToRedis(namespace);
        Toleration toleration = new TolerationBuilder()
                .withKey("dedicated")
                .withOperator("Equal")
                .withValue("debezium")
                .withEffect("NoSchedule")
                .build();
        PodTemplate podTemplate = new PodTemplate();
        podTemplate.setTolerations(List.of(toleration));
        server.getSpec().getRuntime().getTemplates().setPod(podTemplate);

        logger.info("Deploying Debezium Server with tolerations");
        KubeResourceManager.getInstance().createResourceWithWait(server);
        assertStreamingWorks();

        Deployment deployment = KubeResourceManager.getKubeClient().getClient()
                .apps().deployments()
                .inNamespace(namespace)
                .withName(server.getMetadata().getName())
                .get();

        assertThat(deployment).isNotNull();
        assertThat(deployment.getSpec().getTemplate().getSpec().getTolerations())
                .hasSize(1)
                .first()
                .satisfies(t -> {
                    assertThat(t.getKey()).isEqualTo("dedicated");
                    assertThat(t.getOperator()).isEqualTo("Equal");
                    assertThat(t.getValue()).isEqualTo("debezium");
                    assertThat(t.getEffect()).isEqualTo("NoSchedule");
                });
    }
}
