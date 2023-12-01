/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.templates;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.debezium.operator.docs.annotations.Documented;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.SecurityContext;

@JsonPropertyOrder({ "resources", "securityContext" })
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Documented
public class ContainerTemplate implements Serializable {
    public static final long serialVersionUID = 1L;

    @JsonPropertyDescription("Environment variables applied to the container.")
    private List<ContainerEnvVar> env = List.of();
    @JsonPropertyDescription("CPU and memory resource requirements.")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ResourceRequirements resources;

    @JsonPropertyDescription("Container security context.")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private SecurityContext securityContext;

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

    public List<ContainerEnvVar> getEnv() {
        return env;
    }

    public void setEnv(List<ContainerEnvVar> env) {
        this.env = env;
    }
}
