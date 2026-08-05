/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.systemtests.resources.annotations.extensions;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import io.debezium.operator.systemtests.resources.server.DebeziumServerResource;
import io.skodjob.kubetest4j.resources.CustomResourceDefinitionType;
import io.skodjob.kubetest4j.resources.KubeResourceManager;
import io.skodjob.kubetest4j.resources.NamespaceType;

public class DebeziumResourceTypesExtension implements BeforeAllCallback {
    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        KubeResourceManager.get().setResourceTypes(new NamespaceType(), new CustomResourceDefinitionType(), new DebeziumServerResource());
    }
}
