/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.systemtests.resources.operator;

import static io.debezium.operator.systemtests.ConfigProperties.BUNDLE_PATH;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import io.debezium.operator.systemtests.resources.DeployableResourceGroup;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.rbac.ClusterRole;
import io.fabric8.kubernetes.api.model.rbac.RoleBinding;
import io.skodjob.testframe.resources.KubeResourceManager;

public class DebeziumOperatorBundleResource implements DeployableResourceGroup {
    CustomResourceDefinition crd;
    ServiceAccount serviceAccount;
    ClusterRole clusterRole;
    RoleBinding debeziumClusterRoleBinding;
    RoleBinding viewRoleBinding;
    Service service;
    Deployment deployment;

    @Override
    public void configureAsDefault(String namespace) {
        try {
            List<HasMetadata> res = KubeResourceManager.get().kubeClient().readResourcesFromFile(Paths.get(BUNDLE_PATH + "kubernetes.yml"));
            for (HasMetadata object : res) {
                object.getMetadata().setNamespace(namespace);
                switch (object.getKind()) {
                    case "ServiceAccount":
                        this.serviceAccount = (ServiceAccount) object;
                        break;
                    case "ClusterRole":
                        this.clusterRole = (ClusterRole) object;
                        break;
                    case "RoleBinding":
                        if (object.getMetadata().getName().contains("view")) {
                            this.viewRoleBinding = (RoleBinding) object;
                        }
                        else {
                            this.debeziumClusterRoleBinding = (RoleBinding) object;
                        }
                        break;
                    case "Service":
                        this.service = (Service) object;
                        break;
                    default:
                        this.deployment = (Deployment) object;
                        this.deployment.getSpec().getTemplate().getSpec().getContainers().get(0).setImage("quay.io/debezium/operator:nightly");
                        this.deployment.getSpec().getTemplate().getSpec().getContainers().get(0).setImagePullPolicy("IfNotPresent");
                        break;
                }
            }
            res = KubeResourceManager.get().kubeClient().readResourcesFromFile(Paths.get(BUNDLE_PATH + "/debeziumservers.debezium.io-v1.yml"));
            if (res.size() != 1) {
                throw new IOException("Specified file cannot be found or is in wrong format!");
            }
            this.crd = (CustomResourceDefinition) res.get(0);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deploy() {
        KubeResourceManager.get().createOrUpdateResourceWithoutWait(crd, serviceAccount,
                clusterRole, debeziumClusterRoleBinding, viewRoleBinding, service);
        KubeResourceManager.get().createOrUpdateResourceWithWait(deployment);
    }
}
