/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.systemtests.resources.dmt;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.debezium.operator.systemtests.ResourceUtils;
import io.debezium.operator.systemtests.resources.DeployableResourceGroup;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.LocalPortForward;
import io.skodjob.testframe.resources.KubeResourceManager;

public class DmtResource implements DeployableResourceGroup {
    private Deployment deployment;
    private Service service;
    private ConfigMap configMap;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void configureAsDefault(String namespace) {
        this.deployment = ResourceUtils.readYaml(this.getClass().getClassLoader().getResource("dmt/dmt-deployment.yaml"), Deployment.class);
        this.service = ResourceUtils.readYaml(this.getClass().getClassLoader().getResource("dmt/dmt-service.yaml"), Service.class);
        this.configMap = ResourceUtils.readYaml(this.getClass().getClassLoader().getResource("dmt/dmt-configuration.yaml"), ConfigMap.class);
        deployment.getMetadata().setNamespace(namespace);
        service.getMetadata().setNamespace(namespace);
        configMap.getMetadata().setNamespace(namespace);
    }

    @Override
    public void deploy() {
        KubeResourceManager.getInstance().createResourceWithoutWait(this.configMap, this.service);
        KubeResourceManager.getInstance().createResourceWithWait(this.deployment);
    }

    // TODO: The port should be configurable
    public LocalPortForward portForward(int port, String namespace) {
        Map<String, String> labels = new HashMap<>();
        labels.put("app.kubernetes.io/name", "database-manipulation-tool");
        List<String> names = KubeResourceManager.getKubeClient().getClient().pods().inNamespace(namespace)
                .withLabels(labels).list().getItems()
                .stream().map(p -> p.getMetadata().getName()).toList();

        if (names.size() == 1) {
            try {
                int cp = KubeResourceManager.getKubeClient().getClient()
                        .pods().inNamespace(namespace)
                        .withName(names.get(0)).get().getSpec().getContainers().get(0).getPorts().get(0).getContainerPort();

                return KubeResourceManager.getKubeClient().getClient()
                        .pods().inNamespace(namespace)
                        .withName(names.get(0)).portForward(cp, InetAddress.getByAddress(new byte[]{ 127, 0, 0, 1 }), port);
            }
            catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }

        }
        else {
            logger.warn("Found {} pods with DMT label, cannot start port forwarding", names.size());
        }
        return null;
    }
}
