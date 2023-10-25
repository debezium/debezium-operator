/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.dependent;

import io.debezium.operator.DebeziumServer;
import io.debezium.operator.dependent.discriminators.JmxServiceDiscriminator;
import io.debezium.operator.model.CommonLabels;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.ServiceSpecBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;

@KubernetesDependent(resourceDiscriminator = JmxServiceDiscriminator.class)
public class JmxServiceDependent extends CRUDKubernetesDependentResource<Service, DebeziumServer> {

    public static final String JMX_CLASSIFIER = "jmx";

    public JmxServiceDependent() {
        super(Service.class);
    }

    @Override
    protected Service desired(DebeziumServer primary, Context<DebeziumServer> context) {
        var jmxConfig = primary.getSpec().getRuntime().getJmx();

        var name = primary.getMetadata().getName();

        var labels = CommonLabels.serverComponent(name)
                .withDbzClassifier(JMX_CLASSIFIER)
                .getMap();
        var selector = CommonLabels.serverComponent(name)
                .getMap();

        return new ServiceBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withNamespace(primary.getMetadata().getNamespace())
                        .withName(name + "-jmx")
                        .withLabels(labels)
                        .build())
                .withSpec(new ServiceSpecBuilder()
                        .withSelector(selector)
                        .withPorts(new ServicePortBuilder()
                                .withName("jmx")
                                .withProtocol("TCP")
                                .withTargetPort(new IntOrString(jmxConfig.getPort()))
                                .withPort(jmxConfig.getPort())
                                .build())
                        .build())
                .build();
    }
}
