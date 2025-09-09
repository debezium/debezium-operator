/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.systemtests.resources.logs;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.debezium.operator.systemtests.ConfigProperties;
import io.debezium.operator.systemtests.TestBase;
import io.fabric8.kubernetes.api.model.LabelSelectorBuilder;
import io.skodjob.testframe.LogCollector;
import io.skodjob.testframe.LogCollectorBuilder;
import io.skodjob.testframe.interfaces.MustGatherSupplier;
import io.skodjob.testframe.resources.KubeResourceManager;

public class MustGatherImpl implements MustGatherSupplier {
    private static final Logger logger = LoggerFactory.getLogger(MustGatherImpl.class);

    @Override
    public void saveKubernetesState(ExtensionContext extensionContext) {
        LogCollector logCollector = new LogCollectorBuilder()
                .withNamespacedResources(
                        "debeziumserver",
                        "deployment",
                        "secret",
                        "configmap",
                        "role",
                        "rolebinding",
                        "serviceaccount",
                        "pvc",
                        "statefulset",
                        "replicaset",
                        "service",
                        "route",
                        "ingress",
                        "networkpolicy")
                .withClusterWideResources(
                        "node",
                        "pv")
                .withKubeClient(KubeResourceManager.get().kubeClient())
                .withKubeCmdClient(KubeResourceManager.get().kubeCmdClient())
                .withRootFolderPath(getLogPath(
                        TestBase.LOG_DIR.resolve("failedTest").toString(), extensionContext).toString())
                .build();
        try {
            logCollector.collectFromNamespacesWithLabels(new LabelSelectorBuilder()
                    .withMatchLabels(Collections.singletonMap(ConfigProperties.LOG_COLLECT_LABEL, "true"))
                    .build());
        }
        catch (Exception ignored) {
            logger.warn("Failed to collect");
        }
        logCollector.collectClusterWideResources();
    }

    private Path getLogPath(String folderName, ExtensionContext context) {
        String testMethod = context.getDisplayName();
        String testClassName = context.getTestClass().map(Class::getName).orElse("NOCLASS");
        Path path = TestBase.LOG_DIR.resolve(Paths.get(folderName, testClassName));
        if (testMethod != null) {
            path = path.resolve(testMethod.replace("(", "").replace(")", ""));
        }
        return path;
    }
}
