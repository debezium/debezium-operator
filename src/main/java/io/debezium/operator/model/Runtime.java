/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.fabric8.kubernetes.api.model.EnvFromSource;
import io.fabric8.kubernetes.api.model.Volume;

public class Runtime {

    @JsonPropertyDescription("Additional environment variables set from ConfigMaps or Secrets in containers.")
    private List<EnvFromSource> env;

    @JsonPropertyDescription("Additional volumes mounted to containers.")
    private List<Volume> volumes;

    @JsonPropertyDescription("Debezium Server resource templates.")
    private Templates templates;

    public Runtime() {
        this.env = new ArrayList<>();
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
}
