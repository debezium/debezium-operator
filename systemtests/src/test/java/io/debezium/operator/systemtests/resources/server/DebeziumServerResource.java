/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.systemtests.resources.server;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.debezium.operator.api.model.DebeziumServer;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.skodjob.testframe.interfaces.ResourceType;
import io.skodjob.testframe.resources.KubeResourceManager;

public class DebeziumServerResource implements ResourceType<DebeziumServer> {

    private final MixedOperation<DebeziumServer, DebeziumServerList, Resource<DebeziumServer>> client;
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public DebeziumServerResource() {
        this.client = KubeResourceManager.get().kubeClient().getClient().resources(DebeziumServer.class, DebeziumServerList.class);
    }

    public DebeziumServer get(String namespace, String name) {
        return client.inNamespace(namespace).withName(name).get();
    }

    @Override
    public NonNamespaceOperation<?, ?, ?> getClient() {
        return client;
    }

    @Override
    public String getKind() {
        return "DebeziumServer";
    }

    @Override
    public void create(DebeziumServer debeziumServer) {
        client.inNamespace(debeziumServer.getMetadata().getNamespace()).resource(debeziumServer).create();
    }

    @Override
    public void update(DebeziumServer debeziumServer) {
        client.inNamespace(debeziumServer.getMetadata().getNamespace()).resource(debeziumServer).update();
    }

    @Override
    public void delete(DebeziumServer debeziumServer) {
        client.inNamespace(debeziumServer.getMetadata().getNamespace())
                .withName(debeziumServer.getMetadata().getName()).delete();
    }

    @Override
    public void replace(DebeziumServer debeziumServer, Consumer<DebeziumServer> consumer) {
        DebeziumServer toBeUpdated = client.inNamespace(debeziumServer.getMetadata().getNamespace())
                .withName(debeziumServer.getMetadata().getName()).get();
        consumer.accept(toBeUpdated);
        update(toBeUpdated);
    }

    @Override
    public boolean isReady(DebeziumServer debeziumServer) {
        return debeziumServer.getStatus().getConditions().stream()
                .anyMatch(condition -> condition.getType().equals("Ready") && condition.getStatus().equals("True"));
    }

    @Override
    public boolean isDeleted(DebeziumServer debeziumServer) {
        return debeziumServer == null;
    }
}
