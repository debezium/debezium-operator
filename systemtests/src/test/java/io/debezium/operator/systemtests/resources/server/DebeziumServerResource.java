/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.systemtests.resources.server;

import static io.debezium.operator.systemtests.ConfigProperties.FABRIC8_POLL_INTERVAL;
import static io.debezium.operator.systemtests.ConfigProperties.FABRIC8_POLL_TIMEOUT;
import static org.awaitility.Awaitility.await;

import java.io.InputStream;
import java.time.Duration;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.debezium.operator.api.model.DebeziumServer;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperationsImpl;
import io.skodjob.testframe.interfaces.ResourceType;
import io.skodjob.testframe.resources.KubeResourceManager;

public class DebeziumServerResource implements ResourceType<DebeziumServer> {

    private final MixedOperation<DebeziumServer, DebeziumServerList, Resource<DebeziumServer>> client;
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public DebeziumServerResource() {
        this.client = KubeResourceManager.getKubeClient().getClient().resources(DebeziumServer.class, DebeziumServerList.class);
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
    public void delete(String name) {
        client.list().getItems().stream()
                .filter(n -> n.getMetadata().getName().equals(name)).findFirst().ifPresent(client::delete);
    }

    @Override
    public void replace(String s, Consumer<DebeziumServer> editor) {
        DebeziumServer toBeReplaced = client.withName(s).get();
        editor.accept(toBeReplaced);
        update(toBeReplaced);
    }

    @Override
    public boolean waitForReadiness(DebeziumServer debeziumServer) {
        await().atMost(Duration.ofSeconds(FABRIC8_POLL_TIMEOUT)).pollInterval(Duration.ofSeconds(FABRIC8_POLL_INTERVAL))
                .until(() -> {
                    DebeziumServer dbzServer = client.inNamespace(debeziumServer.getMetadata().getNamespace())
                            .withName(debeziumServer.getMetadata().getName()).get();

                    boolean ready = dbzServer.getStatus().getConditions().stream()
                            .anyMatch(condition -> condition.getType().equals("Ready") && condition.getStatus().equals("True"));
                    if (ready) {
                        return true;
                    }
                    else {
                        logger.info("Waiting for readiness of Debezium Server...");
                        return false;
                    }
                });
        return true;
    }

    @Override
    public boolean waitForDeletion(DebeziumServer debeziumServer) {
        return debeziumServer == null;
    }

    public DebeziumServer loadResource(InputStream is) {
        return (DebeziumServer) ((HasMetadataOperationsImpl<?, ?>) this.client.load(is)).getItem();
    }
}
