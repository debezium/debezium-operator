/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.systemtests.resources.sinks;

import java.io.IOException;

import io.debezium.operator.systemtests.resources.DeployableResourceGroup;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.skodjob.testframe.resources.KubeResourceManager;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RedisResource implements DeployableResourceGroup {
    private Pod pod;
    private PersistentVolumeClaim persistentVolumeClaim;
    private Service service;
    private ConfigMap configMap;

    @Override
    public void configureAsDefault(String namespace) {
        try {
            this.pod = (Pod) KubeResourceManager.getKubeClient().readResourcesFromFile(this.getClass().getClassLoader().getResourceAsStream("redis/redis-pod.yaml"))
                    .get(0);
            pod.getMetadata().setNamespace(namespace);
            this.persistentVolumeClaim = (PersistentVolumeClaim) KubeResourceManager.getKubeClient()
                    .readResourcesFromFile(this.getClass().getClassLoader().getResourceAsStream("redis/redis-pvc.yaml")).get(0);
            persistentVolumeClaim.getMetadata().setNamespace(namespace);
            this.configMap = (ConfigMap) KubeResourceManager.getKubeClient()
                    .readResourcesFromFile(this.getClass().getClassLoader().getResourceAsStream("redis/redis-cfg.yaml")).get(0);
            configMap.getMetadata().setNamespace(namespace);
            this.service = (Service) KubeResourceManager.getKubeClient()
                    .readResourcesFromFile(this.getClass().getClassLoader().getResourceAsStream("redis/redis-service.yaml")).get(0);
            service.getMetadata().setNamespace(namespace);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getDefaultRedisAddress() {
        return "redis-service:6379";
    }

    @Override
    public void deploy() {
        KubeResourceManager.getInstance().createResourceWithoutWait(configMap, service, persistentVolumeClaim);
        KubeResourceManager.getInstance().createResourceWithWait(pod);
    }

}
