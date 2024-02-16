/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.runtime.storage;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.debezium.operator.docs.annotations.Documented;
import io.fabric8.kubernetes.api.model.Volume;

@Documented
@JsonPropertyOrder({ "data", "external" })
public class RuntimeStorage {

    @JsonPropertyDescription("File storage configuration used by this instance of Debezium Server.")
    private DataStorage data = new DataStorage();

    @JsonPropertyDescription("Additional volumes mounted to /debezium/external")
    @Documented.Field(k8Ref = "volume-v1-core")
    private List<Volume> external = List.of();

    public DataStorage getData() {
        return data;
    }

    public void setData(DataStorage data) {
        this.data = data;
    }

    public List<Volume> getExternal() {
        return external;
    }

    public void setExternal(List<Volume> external) {
        this.external = external;
    }
}
