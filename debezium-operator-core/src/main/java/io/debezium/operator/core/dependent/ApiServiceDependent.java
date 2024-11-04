/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core.dependent;

import io.debezium.operator.api.model.CommonLabels;
import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.core.dependent.discriminators.ApiServiceDiscriminator;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.ServiceSpecBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;

@KubernetesDependent(resourceDiscriminator = ApiServiceDiscriminator.class)
public class ApiServiceDependent extends CRUDKubernetesDependentResource<Service, DebeziumServer> {

    public static final String CLASSIFIER = "api";
    public static final int API_PORT = DeploymentDependent.DEFAULT_HTTP_PORT;

    public ApiServiceDependent() {
        super(Service.class);
    }

    @Override
    protected Service desired(DebeziumServer primary, Context<DebeziumServer> context) {
        var name = primary.getMetadata().getName();

        var labels = CommonLabels.serverComponent(name)
                .withDbzClassifier(CLASSIFIER)
                .getMap();
        var selector = CommonLabels.serverComponent(name)
                .getMap();

        return new ServiceBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withNamespace(primary.getMetadata().getNamespace())
                        .withName(name + "-api")
                        .withLabels(labels)
                        .build())
                .withSpec(new ServiceSpecBuilder()
                        .withSelector(selector)
                        .withPorts(new ServicePortBuilder()
                                .withName("http")
                                .withProtocol("TCP")
                                .withTargetPort(new IntOrString(DeploymentDependent.DEFAULT_HTTP_PORT))
                                .withPort(API_PORT)
                                .build())
                        .build())
                .build();
    }
}
