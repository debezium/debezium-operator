/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.systemtests.resources.annotations.extensions;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import io.debezium.operator.systemtests.ConfigProperties;
import io.debezium.operator.systemtests.resources.server.DebeziumServerResource;
import io.skodjob.testframe.resources.ConfigMapType;
import io.skodjob.testframe.resources.CustomResourceDefinitionType;
import io.skodjob.testframe.resources.DeploymentType;
import io.skodjob.testframe.resources.KubeResourceManager;
import io.skodjob.testframe.resources.NamespaceType;
import io.skodjob.testframe.resources.ServiceType;
import io.skodjob.testframe.utils.KubeUtils;

public class DebeziumResourceTypesExtension implements BeforeAllCallback {
    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        KubeResourceManager.get().setResourceTypes(
                new NamespaceType(),
                new CustomResourceDefinitionType(),
                new DebeziumServerResource(),
                new DeploymentType(),
                new ServiceType(),
                new ConfigMapType());

        KubeResourceManager.get().addCreateCallback(r -> {
            if (r.getKind().equals("Namespace")) {
                KubeUtils.labelNamespace(r.getMetadata().getName(), ConfigProperties.LOG_COLLECT_LABEL, "true");
            }
        });
    }
}
