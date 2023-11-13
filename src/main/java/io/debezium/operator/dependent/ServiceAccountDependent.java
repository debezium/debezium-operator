/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.dependent;

import java.util.Objects;

import io.debezium.operator.DebeziumServer;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;

public class ServiceAccountDependent
        extends CRUDKubernetesDependentResource<ServiceAccount, DebeziumServer> {

    private static final String MANAGED_SA_NAME_TEMPLATE = "%s-sa";

    public ServiceAccountDependent() {
        super(ServiceAccount.class);
    }

    @Override
    protected ServiceAccount desired(DebeziumServer primary, Context<DebeziumServer> context) {
        var saName = managedServiceAccountNameFor(primary);

        return new ServiceAccountBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName(saName)
                        .withNamespace(primary.getMetadata().getNamespace())
                        .build())
                .build();
    }

    private static String managedServiceAccountNameFor(DebeziumServer primary) {
        var name = primary.getMetadata().getName();
        return MANAGED_SA_NAME_TEMPLATE.formatted(name);
    }

    public static String serviceAccountNameFor(DebeziumServer primary) {
        var runtime = primary.getSpec().getRuntime();
        var saName = runtime.getServiceAccount();
        var managedSaName = managedServiceAccountNameFor(primary);

        return Objects.requireNonNullElse(saName, managedSaName);
    }
}
