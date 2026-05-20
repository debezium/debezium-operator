/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.runtime.templates;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.debezium.operator.docs.annotations.Documented;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.SecurityContext;

@JsonPropertyOrder({ "resources", "securityContext", "probes", "imagePullPolicy" })
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Documented
public class ContainerTemplate implements Serializable {
    public static final long serialVersionUID = 1L;

    @JsonPropertyDescription("CPU and memory resource requirements.")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Documented.Field(k8Ref = "resourcerequirements-v1-core")
    private ResourceRequirements resources;

    @JsonPropertyDescription("Container security context.")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Documented.Field(k8Ref = "securitycontext-v1-core")
    private SecurityContext securityContext;

    @JsonPropertyDescription("Container probes configuration.")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Probes probes = new Probes();

    @JsonPropertyDescription("Image pull policy for the container.")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ImagePullPolicy imagePullPolicy;

    public SecurityContext getSecurityContext() {
        return securityContext;
    }

    public void setSecurityContext(SecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    public ResourceRequirements getResources() {
        return resources;
    }

    public void setResources(ResourceRequirements resources) {
        this.resources = resources;
    }

    public Probes getProbes() {
        return probes;
    }

    public void setProbes(Probes probes) {
        this.probes = probes;
    }

    public ImagePullPolicy getImagePullPolicy() {
        return imagePullPolicy;
    }

    public void setImagePullPolicy(ImagePullPolicy imagePullPolicy) {
        this.imagePullPolicy = imagePullPolicy;
    }
}
