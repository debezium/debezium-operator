/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.dependent;

import java.util.Map;

import io.debezium.operator.DebeziumServer;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.ServiceSpecBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;

public class ServiceDependent extends CRUDKubernetesDependentResource<Service, DebeziumServer> {

    public ServiceDependent() {
        super(Service.class);
    }

    @Override
    protected Service desired(DebeziumServer primary, Context<DebeziumServer> context) {
        var name = primary.getMetadata().getName();
        var labels = Map.of(
                "app", name,
                "deployment", name);

        context.getSecondaryResource(Deployment.class);

        return new ServiceBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName(name)
                        .withNamespace(primary.getMetadata().getNamespace())
                        .withLabels(labels)
                        .build())
                .withSpec(new ServiceSpecBuilder()
                        .withSelector(labels)
                        .withPorts(new ServicePortBuilder()
                                .withName("http")
                                .withProtocol("TCP")
                                .withPort(DeploymentDependent.DEFAULT_HTTP_PORT)
                                .build())
                        .build())
                .build();
    }
}
