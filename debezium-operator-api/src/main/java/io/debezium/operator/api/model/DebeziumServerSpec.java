/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.debezium.operator.api.config.ConfigMappable;
import io.debezium.operator.api.config.ConfigMapping;
import io.debezium.operator.api.model.runtime.Runtime;
import io.debezium.operator.docs.annotations.Documented;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Documented
@JsonPropertyOrder({ "image", "version", "image", "quarkus", "runtime", "format", "transforms", "predicates", "source", "sink" })
public class DebeziumServerSpec implements ConfigMappable {

    @JsonPropertyDescription("Image used for Debezium Server container. This property takes precedence over version.")
    private String image;

    @JsonPropertyDescription("Version of Debezium Server to be used.")
    @Documented.Field(defaultValue = "same as operator")
    private String version;

    @JsonPropertyDescription("Sink configuration.")
    private Sink sink;

    @JsonPropertyDescription("Debezium source connector configuration.")
    private Source source;

    @JsonPropertyDescription("Message output format configuration.")
    private Format format;

    @JsonPropertyDescription("Quarkus configuration passed down to Debezium Server process.")
    private Quarkus quarkus;

    @JsonPropertyDescription("Configuration allowing the modification of various aspects of Debezium Server runtime.")
    private Runtime runtime;

    @JsonPropertyDescription("Single Message Transformations employed by this instance of Debezium Server.")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Transformation> transforms;

    @JsonPropertyDescription("Predicates employed by this instance of Debezium Server.")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Predicate> predicates;

    public DebeziumServerSpec() {
        this.sink = new Sink();
        this.source = new Source();
        this.format = new Format();
        this.quarkus = new Quarkus();
        this.runtime = new Runtime();
        this.transforms = List.of();
        this.predicates = Map.of();
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Sink getSink() {
        return sink;
    }

    public void setSink(Sink sink) {
        this.sink = sink;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public Quarkus getQuarkus() {
        return quarkus;
    }

    public void setQuarkus(Quarkus quarkus) {
        this.quarkus = quarkus;
    }

    public Runtime getRuntime() {
        return runtime;
    }

    public void setRuntime(Runtime runtime) {
        this.runtime = runtime;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public List<Transformation> getTransforms() {
        return transforms;
    }

    public void setTransforms(List<Transformation> transforms) {
        this.transforms = transforms;
    }

    public Map<String, Predicate> getPredicates() {
        return predicates;
    }

    public void setPredicates(Map<String, Predicate> predicates) {
        this.predicates = predicates;
    }

    @Override
    public ConfigMapping asConfiguration() {
        var dbzConfig = ConfigMapping.prefixed("debezium");
        dbzConfig.putAll("source", source);
        dbzConfig.putAll("sink", sink);
        dbzConfig.putAll("format", format);
        dbzConfig.putList("transforms", transforms, "t");
        dbzConfig.putMap("predicates", predicates);

        var config = ConfigMapping.empty();
        config.putAll("quarkus", quarkus);
        config.putAll(dbzConfig);

        return config;
    }
}
