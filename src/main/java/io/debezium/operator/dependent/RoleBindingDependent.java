/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.dependent;

import io.debezium.operator.DebeziumServer;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.rbac.RoleBinding;
import io.fabric8.kubernetes.api.model.rbac.RoleBindingBuilder;
import io.fabric8.kubernetes.api.model.rbac.RoleRefBuilder;
import io.fabric8.kubernetes.api.model.rbac.SubjectBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;

public class RoleBindingDependent
        extends CRUDKubernetesDependentResource<RoleBinding, DebeziumServer> {
    public static final String ROLE_NAME = "config-view";
    public static final String BINDING_NAME = "%s-" + ROLE_NAME;

    public RoleBindingDependent() {
        super(RoleBinding.class);
    }

    @Override
    protected RoleBinding desired(DebeziumServer primary, Context<DebeziumServer> context) {
        var sa = context.getSecondaryResource(ServiceAccount.class)
                .map(r -> r.getMetadata().getName())
                .orElseThrow();

        return new RoleBindingBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName(BINDING_NAME.formatted(sa))
                        .withNamespace(primary.getMetadata().getNamespace())
                        .build())
                .withRoleRef(new RoleRefBuilder()
                        .withKind("ClusterRole")
                        .withName(ROLE_NAME)
                        .build())
                .withSubjects(new SubjectBuilder()
                        .withKind("ServiceAccount")
                        .withName(sa)
                        .build())
                .build();
    }
}
