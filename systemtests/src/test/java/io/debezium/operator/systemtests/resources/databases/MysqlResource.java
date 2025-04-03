/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.systemtests.resources.databases;

import java.util.HashMap;

import org.apache.commons.lang3.RandomStringUtils;

import io.debezium.operator.systemtests.ResourceUtils;
import io.debezium.operator.systemtests.resources.DeployableResourceGroup;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.skodjob.testframe.resources.KubeResourceManager;

public class MysqlResource implements DeployableResourceGroup {
    private Deployment deployment;
    private PersistentVolumeClaim persistentVolumeClaim;
    private Service service;
    private Secret credentials;

    private final String image = "quay.io/debezium/example-mysql-master:";

    public MysqlResource() {
    }

    public MysqlResource(Deployment deployment, PersistentVolumeClaim persistentVolumeClaim, Service service, Secret credentials) {
        this.deployment = deployment;
        this.persistentVolumeClaim = persistentVolumeClaim;
        this.service = service;
        this.credentials = credentials;
    }

    public Deployment getDeployment() {
        return deployment;
    }

    public void setDeployment(Deployment deployment) {
        this.deployment = deployment;
    }

    public PersistentVolumeClaim getPersistentVolumeClaim() {
        return persistentVolumeClaim;
    }

    public void setPersistentVolumeClaim(PersistentVolumeClaim persistentVolumeClaim) {
        this.persistentVolumeClaim = persistentVolumeClaim;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Secret getCredentials() {
        return credentials;
    }

    public void setCredentials(Secret credentials) {
        this.credentials = credentials;
    }

    public String getImage() {
        return image;
    }

    private Deployment setName(Deployment deployment, String name, String namespace) {
        return new DeploymentBuilder(deployment)
                .editMetadata()
                .withName(name)
                .withNamespace(namespace)
                .endMetadata()
                .build();
    }

    private Deployment setVersion(Deployment deployment, String version) {
        return new DeploymentBuilder(deployment)
                .editSpec()
                .editTemplate()
                .editSpec()
                .editContainer(0)
                .withImage(image + version)
                .endContainer()
                .endSpec()
                .endTemplate()
                .endSpec()
                .build();
    }

    private Deployment setCredentials(Deployment deployment, String password, String rootPassword, String mysqlUser) {
        return new DeploymentBuilder(deployment)
                .editSpec()
                .editTemplate()
                .editSpec()
                .editContainer(0)
                .addNewEnv()
                .withName("MYSQL_PASSWORD")
                .withValue(password)
                .endEnv()
                .addNewEnv()
                .withName("MYSQL_ROOT_PASSWORD")
                .withValue(rootPassword)
                .endEnv()
                .addNewEnv()
                .withName("MYSQL_USER")
                .withValue(mysqlUser)
                .endEnv()
                .endContainer()
                .endSpec()
                .endTemplate()
                .endSpec()
                .build();
    }

    private Deployment setClaimName(Deployment deployment, String pcvName) {
        return new DeploymentBuilder(deployment)
                .editSpec()
                .editTemplate()
                .editSpec()
                .editVolume(0)
                .withNewPersistentVolumeClaim()
                .withClaimName(pcvName)
                .endPersistentVolumeClaim()
                .endVolume()
                .endSpec()
                .endTemplate()
                .endSpec()
                .build();
    }

    private Service createNewService(String serviceName, String namespace) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("app", "mysql");
        hashMap.put("test-component", "database");
        hashMap.put("database-type", "mysql");
        Service service = new ServiceBuilder()
                .withNewMetadata()
                .withNamespace(namespace)
                .withName(serviceName)
                .endMetadata()
                .withNewSpec()
                .withSelector(hashMap)
                .addNewPort()
                .withName("mysql-port")
                .withProtocol("TCP")
                .withPort(3306)
                .withTargetPort(new IntOrString(3306))
                .endPort()
                .endSpec().build();
        return service;
    }

    public MysqlResource configureAsDefault(String version, String namespace) {
        String mysqlName = "mysql-" + RandomStringUtils.random(5, 0, 70, true, false).toLowerCase();
        String pvcName = "mysql-pvc-" + RandomStringUtils.random(5, 0, 70, true, false).toLowerCase();
        String svcName = "mysql-svc";

        Deployment base = ResourceUtils.readYaml(this.getClass().getClassLoader().getResource("mysql/mysql-base.yaml"), Deployment.class);
        base = setName(base, mysqlName, namespace);
        base = setVersion(base, version);
        base = setCredentials(base, "mysqlpw", "debezium", "mysqluser");
        base = setClaimName(base, pvcName);

        PersistentVolumeClaim pvc = ResourceUtils.readYaml(this.getClass().getClassLoader().getResource("mysql/mysql-pvc.yaml"), PersistentVolumeClaim.class);
        pvc.getMetadata().setNamespace(namespace);
        pvc = new PersistentVolumeClaimBuilder(pvc).editMetadata().withName(pvcName).withNamespace(namespace).endMetadata().build();
        Service mysqlService = createNewService(svcName, namespace);

        Secret cred = ResourceUtils.readYaml(this.getClass().getClassLoader().getResource("mysql/mysql-credentials.yaml"), Secret.class);
        cred.getMetadata().setNamespace(namespace);

        return new MysqlResource(base, pvc, mysqlService, cred);
    }

    @Override
    public void configureAsDefault(String namespace) {
        Deployment base = ResourceUtils.readYaml(this.getClass().getClassLoader().getResource("mysql/mysql-base.yaml"), Deployment.class);
        base = setName(base, "mysql-dbz", namespace);
        base = setVersion(base, "2.7.0.Alpha1");
        base = setCredentials(base, "mysqlpw", "debezium", "mysqluser");
        base = setClaimName(base, "mysql-dbz-pvc");
        this.deployment = base;
        this.deployment.getMetadata().setNamespace(namespace);
        PersistentVolumeClaim pvc = ResourceUtils.readYaml(this.getClass().getClassLoader().getResource("mysql/mysql-pvc.yaml"), PersistentVolumeClaim.class);
        this.credentials = ResourceUtils.readYaml(this.getClass().getClassLoader().getResource("mysql/mysql-secret.yaml"), Secret.class);
        this.credentials.getMetadata().setNamespace(namespace);

        pvc.getMetadata().setNamespace(namespace);
        this.persistentVolumeClaim = new PersistentVolumeClaimBuilder(pvc).editMetadata().withName("mysql-dbz-pvc").withNamespace(namespace).endMetadata().build();
        this.service = createNewService("mysql-dbz-svc", namespace);
    }

    public void deploy() {
        KubeResourceManager.getInstance().createResourceWithoutWait(this.persistentVolumeClaim, this.credentials, this.service);
        KubeResourceManager.getInstance().createResourceWithWait(this.deployment);
    }
}
