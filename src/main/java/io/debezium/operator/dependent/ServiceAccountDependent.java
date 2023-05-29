/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.dependent;

import io.debezium.operator.DebeziumServer;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;

public class ServiceAccountDependent
        extends CRUDKubernetesDependentResource<ServiceAccount, DebeziumServer> {

    public static final String SA_NAME = "%s-debezium-server";

    public ServiceAccountDependent() {
        super(ServiceAccount.class);
    }

    @Override
    protected ServiceAccount desired(DebeziumServer primary, Context<DebeziumServer> context) {
        return new ServiceAccountBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName(SA_NAME.formatted(primary.getMetadata().getName()))
                        .withNamespace(primary.getMetadata().getNamespace())
                        .build())
                .build();
    }
}
