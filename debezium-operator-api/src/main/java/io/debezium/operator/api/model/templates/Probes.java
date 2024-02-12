/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.templates;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.debezium.operator.docs.annotations.Documented;

@JsonPropertyOrder({ "readiness", "liveness" })
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Documented
public class Probes {

    @JsonPropertyDescription("Readiness probe configuration applied to the container.")
    private Probe readiness;
    @JsonPropertyDescription("Liveness probe configuration applied to the container.")
    private Probe liveness;

    public Probes() {
        this.readiness = new Probe();
        this.liveness = new Probe();
    }

    public Probe getReadiness() {
        return readiness;
    }

    public void setReadiness(Probe readiness) {
        this.readiness = readiness;
    }

    public Probe getLiveness() {
        return liveness;
    }

    public void setLiveness(Probe liveness) {
        this.liveness = liveness;
    }
}
