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

public class RedisResource implements DeployableResourceGroup {
    private Pod pod;
    private PersistentVolumeClaim persistentVolumeClaim;
    private Service service;
    private ConfigMap configMap;

    public RedisResource() {
    }

    public RedisResource(Pod pod, PersistentVolumeClaim persistentVolumeClaim, Service service, ConfigMap configMap) {
        this.pod = pod;
        this.persistentVolumeClaim = persistentVolumeClaim;
        this.service = service;
        this.configMap = configMap;
    }

    public Pod getPod() {
        return pod;
    }

    public void setPod(Pod pod) {
        this.pod = pod;
    }

    public PersistentVolumeClaim getPersistentVolumeClaim() {
        return persistentVolumeClaim;
    }

    public void setPersistentVolumeClaim(PersistentVolumeClaim persistentVolumeClaim) {
        this.persistentVolumeClaim = persistentVolumeClaim;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public ConfigMap getConfigMap() {
        return configMap;
    }

    public void setConfigMap(ConfigMap configMap) {
        this.configMap = configMap;
    }

    @Override
    public void configureAsDefault(String namespace) {
        try {
            this.pod = (Pod) KubeResourceManager.get().kubeClient().readResourcesFromFile(this.getClass().getClassLoader().getResourceAsStream("redis/redis-pod.yaml"))
                    .get(0);
            pod.getMetadata().setNamespace(namespace);
            this.persistentVolumeClaim = (PersistentVolumeClaim) KubeResourceManager.get().kubeClient()
                    .readResourcesFromFile(this.getClass().getClassLoader().getResourceAsStream("redis/redis-pvc.yaml")).get(0);
            persistentVolumeClaim.getMetadata().setNamespace(namespace);
            this.configMap = (ConfigMap) KubeResourceManager.get().kubeClient()
                    .readResourcesFromFile(this.getClass().getClassLoader().getResourceAsStream("redis/redis-cfg.yaml")).get(0);
            configMap.getMetadata().setNamespace(namespace);
            this.service = (Service) KubeResourceManager.get().kubeClient()
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
        KubeResourceManager.get().createResourceWithoutWait(configMap, service, persistentVolumeClaim);
        KubeResourceManager.get().createResourceWithWait(pod);
    }

}
