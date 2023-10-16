/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.debezium.operator.model.templates.PodTemplate;

public class Templates {

    @JsonPropertyDescription("Pod template.")
    private PodTemplate pod;

    public Templates() {
        this.pod = new PodTemplate();
    }

    public PodTemplate getPod() {
        return pod;
    }

    public void setPod(PodTemplate pod) {
        this.pod = pod;
    }
}
