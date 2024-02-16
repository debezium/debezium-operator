/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.runtime.templates;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.debezium.operator.docs.annotations.Documented;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimSpec;

@JsonPropertyOrder({ "container", "pod", "volumeClaim" })
@Documented
public class Templates {

    @JsonPropertyDescription("Container template")
    private ContainerTemplate container;
    @JsonPropertyDescription("Pod template.")
    private PodTemplate pod;

    @JsonPropertyDescription("PVC template for data volume if no explicit claim is specified.")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Documented.Field(k8Ref = "persistentvolumeclaimspec-v1-core")
    private PersistentVolumeClaimSpec volumeClaim;

    public Templates() {
        this.pod = new PodTemplate();
        this.container = new ContainerTemplate();
    }

    public PodTemplate getPod() {
        return pod;
    }

    public void setPod(PodTemplate pod) {
        this.pod = pod;
    }

    public ContainerTemplate getContainer() {
        return container;
    }

    public void setContainer(ContainerTemplate container) {
        this.container = container;
    }

    public PersistentVolumeClaimSpec getVolumeClaim() {
        return volumeClaim;
    }

    public void setVolumeClaim(PersistentVolumeClaimSpec volumeClaim) {
        this.volumeClaim = volumeClaim;
    }
}
