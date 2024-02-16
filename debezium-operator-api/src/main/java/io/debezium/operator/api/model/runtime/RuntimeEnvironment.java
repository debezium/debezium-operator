/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.runtime;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.debezium.operator.api.model.runtime.templates.ContainerEnvVar;
import io.debezium.operator.docs.annotations.Documented;
import io.fabric8.kubernetes.api.model.EnvFromSource;

@JsonPropertyOrder({ "vars", "from" })
@Documented
public class RuntimeEnvironment {

    @JsonPropertyDescription("Environment variables applied to the container.")
    private List<ContainerEnvVar> vars = List.of();

    @JsonPropertyDescription("Additional environment variables set from ConfigMaps or Secrets in containers.")
    @Documented.Field(k8Ref = "envfromsource-v1-core")
    private List<EnvFromSource> from = List.of();

    public List<ContainerEnvVar> getVars() {
        return vars;
    }

    public void setVars(List<ContainerEnvVar> vars) {
        this.vars = vars;
    }

    public List<EnvFromSource> getFrom() {
        return from;
    }

    public void setFrom(List<EnvFromSource> from) {
        this.from = from;
    }
}
