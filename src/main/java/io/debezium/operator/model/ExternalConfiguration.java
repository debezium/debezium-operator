/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.model;

import java.util.ArrayList;
import java.util.List;

import io.fabric8.kubernetes.api.model.EnvFromSource;
import io.fabric8.kubernetes.api.model.Volume;

public class ExternalConfiguration {

    private List<EnvFromSource> env;

    private List<Volume> volumes;

    public ExternalConfiguration() {
        this.env = new ArrayList<>();
        this.volumes = new ArrayList<>();
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
}
