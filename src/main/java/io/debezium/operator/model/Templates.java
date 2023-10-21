/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.debezium.operator.model.templates.ContainerTemplate;
import io.debezium.operator.model.templates.PodTemplate;

@JsonPropertyOrder({ "container", "pod" })
public class Templates {

    @JsonPropertyDescription("Container template")
    private ContainerTemplate container;
    @JsonPropertyDescription("Pod template.")
    private PodTemplate pod;

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
}
