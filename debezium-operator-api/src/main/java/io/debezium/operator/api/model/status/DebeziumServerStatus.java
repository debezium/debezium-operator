/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.status;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.debezium.operator.docs.annotations.Documented;
import io.sundr.builder.annotations.Buildable;

@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder", lazyCollectionInitEnabled = false)
@Documented
public class DebeziumServerStatus {

    @JsonPropertyDescription("List of status conditions")
    private List<Condition> conditions;

    @JsonPropertyDescription("Latest observed generation")
    @JsonProperty(defaultValue = "0L")
    private Long observedGeneration;

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public void addToConditions(Condition condition) {
        if (conditions == null) {
            conditions = new ArrayList<>();
        }
        conditions.add(condition);
    }

    public Long getObservedGeneration() {
        return observedGeneration;
    }

    public void setObservedGeneration(Long observedGeneration) {
        this.observedGeneration = observedGeneration;
    }
}
