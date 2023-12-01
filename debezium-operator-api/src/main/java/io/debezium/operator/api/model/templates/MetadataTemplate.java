/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.templates;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.debezium.operator.docs.annotations.Documented;

@JsonPropertyOrder({ "labels", "annotations" })
@Documented
public class MetadataTemplate implements Serializable {
    public static final long serialVersionUID = 1L;

    @JsonPropertyDescription("Labels added to the Kubernetes resource")
    private Map<String, String> labels = new HashMap<>(0);
    @JsonPropertyDescription("Annotations added to the Kubernetes resource")
    private Map<String, String> annotations = new HashMap<>(0);

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public Map<String, String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Map<String, String> annotations) {
        this.annotations = annotations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MetadataTemplate that)) {
            return false;
        }
        return Objects.equals(labels, that.labels) && Objects.equals(annotations, that.annotations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(labels, annotations);
    }
}
