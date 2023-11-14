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

import io.fabric8.kubernetes.api.model.Affinity;
import io.fabric8.kubernetes.api.model.LocalObjectReference;
import io.fabric8.kubernetes.api.model.PodSecurityContext;

@JsonPropertyOrder({ "metadata", "imagePullSecrets", "affinity" })
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class PodTemplate implements HasMetadataTemplate, Serializable {

    public static final long serialVersionUID = 1L;

    @JsonPropertyDescription("Metadata applied to the resource.")
    private MetadataTemplate metadata = new MetadataTemplate();

    @JsonPropertyDescription("List of local references to secrets used for pulling any of the images used by this Pod.")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<LocalObjectReference> imagePullSecrets = List.of();

    @JsonPropertyDescription("Pod affinity rules")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Affinity affinity;

    @JsonPropertyDescription("Pod-level security attributes and container settings")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PodSecurityContext securityContext;

    @Override
    public MetadataTemplate getMetadata() {
        return metadata;
    }

    @Override
    public void setMetadata(MetadataTemplate metadata) {
        this.metadata = metadata;
    }

    public List<LocalObjectReference> getImagePullSecrets() {
        return imagePullSecrets;
    }

    public void setImagePullSecrets(List<LocalObjectReference> imagePullSecrets) {
        this.imagePullSecrets = imagePullSecrets;
    }

    public Affinity getAffinity() {
        return affinity;
    }

    public void setAffinity(Affinity affinity) {
        this.affinity = affinity;
    }

    public PodSecurityContext getSecurityContext() {
        return securityContext;
    }

    public void setSecurityContext(PodSecurityContext securityContext) {
        this.securityContext = securityContext;
    }
}
