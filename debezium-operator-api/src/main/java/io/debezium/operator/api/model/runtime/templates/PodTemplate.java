/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.runtime.templates;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.debezium.operator.docs.annotations.Documented;
import io.fabric8.kubernetes.api.model.Affinity;
import io.fabric8.kubernetes.api.model.LocalObjectReference;
import io.fabric8.kubernetes.api.model.PodSecurityContext;
import io.fabric8.kubernetes.api.model.Toleration;

@JsonPropertyOrder({ "metadata", "imagePullSecrets", "affinity", "nodeSelector", "tolerations" })
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Documented
public class PodTemplate implements HasMetadataTemplate, Serializable {

    public static final long serialVersionUID = 1L;

    @JsonPropertyDescription("Metadata applied to the resource.")
    private MetadataTemplate metadata = new MetadataTemplate();

    @JsonPropertyDescription("List of local references to secrets used for pulling any of the images used by this Pod.")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Documented.Field(k8Ref = "localobjectreference-v1-core")
    private List<LocalObjectReference> imagePullSecrets = List.of();

    @JsonPropertyDescription("Pod affinity rules")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Documented.Field(k8Ref = "affinity-v1-core")
    private Affinity affinity;

    @JsonPropertyDescription("Pod-level security attributes and container settings")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Documented.Field(k8Ref = "podsecuritycontext-v1-core")
    private PodSecurityContext securityContext;

    @JsonPropertyDescription("NodeSelector is a selector which must be true for the pod to fit on a node.")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> nodeSelector = Map.of();

    @JsonPropertyDescription("Tolerations applied to the pod to allow scheduling on tainted nodes.")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Documented.Field(k8Ref = "toleration-v1-core")
    private List<Toleration> tolerations = List.of();

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

    public Map<String, String> getNodeSelector() {
        return nodeSelector;
    }

    public void setNodeSelector(Map<String, String> nodeSelector) {
        this.nodeSelector = nodeSelector;
    }

    public List<Toleration> getTolerations() {
        return tolerations;
    }

    public void setTolerations(List<Toleration> tolerations) {
        this.tolerations = tolerations;
    }
}
