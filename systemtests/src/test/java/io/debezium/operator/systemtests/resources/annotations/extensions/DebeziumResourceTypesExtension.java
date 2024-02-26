/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.systemtests.resources.annotations.extensions;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import io.debezium.operator.systemtests.resources.server.DebeziumServerResource;
import io.skodjob.testframe.resources.CustomResourceDefinitionType;
import io.skodjob.testframe.resources.KubeResourceManager;
import io.skodjob.testframe.resources.NamespaceType;

public class DebeziumResourceTypesExtension implements BeforeAllCallback {
    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        KubeResourceManager.getInstance().setResourceTypes(new NamespaceType(), new CustomResourceDefinitionType(), new DebeziumServerResource());
    }
}
