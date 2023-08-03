/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.dependent;

import io.debezium.operator.DebeziumServer;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.rbac.PolicyRuleBuilder;
import io.fabric8.kubernetes.api.model.rbac.Role;
import io.fabric8.kubernetes.api.model.rbac.RoleBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;

public class RoleDependent
        extends CRUDKubernetesDependentResource<Role, DebeziumServer> {
    public static final String ROLE_NAME = "%s-config-view";

    public RoleDependent() {
        super(Role.class);
    }

    @Override
    protected Role desired(DebeziumServer primary, Context<DebeziumServer> context) {
        return new RoleBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName(ROLE_NAME.formatted(primary.getMetadata().getName()))
                        .withNamespace(primary.getMetadata().getNamespace())
                        .build())
                .withRules(new PolicyRuleBuilder()
                        .withApiGroups("")
                        .withResources("secrets", "configmaps")
                        .withVerbs("get", "list", "watch")
                        .build())
                .build();
    }
}
