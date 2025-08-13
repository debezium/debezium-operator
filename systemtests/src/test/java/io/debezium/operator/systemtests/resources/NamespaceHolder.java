/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.systemtests.resources;

import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.RandomStringUtils;

import io.debezium.operator.systemtests.resources.dmt.DmtResource;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.skodjob.testframe.resources.KubeResourceManager;

public enum NamespaceHolder {
    INSTANCE;

    private String currentNamespace = null;
    private DmtResource namespacedDmt;

    public void createNewNamespace() {
        String name = "dbz-" + RandomStringUtils.random(7, 0, 70, true, false).toLowerCase();
        Namespace namespace = new NamespaceBuilder()
                .withNewMetadata()
                .withName(name)
                .withLabels(Map.of("app", "debezium-systemtests"))
                .endMetadata()
                .build();
        this.currentNamespace = name;
        KubeResourceManager.get().createResourceWithWait(namespace);
    }

    public String getCurrentNamespace() {
        if (Objects.isNull(currentNamespace)) {
            createNewNamespace();
        }
        return currentNamespace;
    }

    public void resetNamespace() {
        this.currentNamespace = null;
    }

    public DmtResource getNamespacedDmt() {
        return namespacedDmt;
    }

    public void setNamespacedDmt(DmtResource namespacedDmt) {
        this.namespacedDmt = namespacedDmt;
    }
}
