/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core.dependent;

import java.util.ArrayList;
import java.util.List;

import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.api.model.source.storage.offset.ConfigMapOffsetStore;
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
                        .withResources("secrets")
                        .withVerbs("get", "list", "watch")
                        .build(),
                        new PolicyRuleBuilder()
                                .withApiGroups("")
                                .withResources("configmaps")
                                .withVerbs(getConfigMapPermissions(primary))
                                .build())
                .build();
    }

    private static List<String> getConfigMapPermissions(DebeziumServer primary) {

        List<String> configMapPermissions = new ArrayList<>(List.of("get", "list", "watch"));
        if (primary.getSpec().getSource().getOffset().getActiveStore() instanceof ConfigMapOffsetStore) {
            configMapPermissions.addAll(List.of("create", "update", "patch"));
        }
        return configMapPermissions;
    }
}
