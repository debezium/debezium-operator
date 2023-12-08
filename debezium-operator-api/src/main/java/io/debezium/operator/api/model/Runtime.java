/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.debezium.operator.docs.annotations.Documented;
import io.fabric8.kubernetes.api.model.EnvFromSource;
import io.fabric8.kubernetes.api.model.Volume;

@JsonPropertyOrder({ "env", "jmx", "templates", "volumes" })
@Documented
public class Runtime {

    @JsonPropertyDescription("Additional environment variables set from ConfigMaps or Secrets in containers.")
    @Documented.Field(k8Ref = "envfromsource-v1-core")
    private List<EnvFromSource> env;

    @JsonPropertyDescription("JMX configuration.")
    private JmxConfig jmx;

    @JsonPropertyDescription("Debezium Server resource templates.")
    private Templates templates;

    @JsonPropertyDescription("Additional volumes mounted to containers.")
    @Documented.Field(k8Ref = "volume-v1-core")
    private List<Volume> volumes;

    @JsonPropertyDescription("An existing service account used to run the Debezium Server pod")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String serviceAccount;

    public Runtime() {
        this.env = new ArrayList<>();
        this.jmx = new JmxConfig();
        this.volumes = new ArrayList<>();
        this.templates = new Templates();
    }

    public List<EnvFromSource> getEnv() {
        return env;
    }

    public void setEnv(List<EnvFromSource> env) {
        this.env = env;
    }

    public List<Volume> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<Volume> volumes) {
        this.volumes = volumes;
    }

    public Templates getTemplates() {
        return templates;
    }

    public void setTemplates(Templates templates) {
        this.templates = templates;
    }

    public JmxConfig getJmx() {
        return jmx;
    }

    public void setJmx(JmxConfig jmx) {
        this.jmx = jmx;
    }

    public String getServiceAccount() {
        return serviceAccount;
    }

    public void setServiceAccount(String serviceAccount) {
        this.serviceAccount = serviceAccount;
    }
}
