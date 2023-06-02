/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.model;

import java.util.List;
import java.util.Map;

import io.debezium.operator.config.ConfigMappable;
import io.debezium.operator.config.ConfigMapping;

public class DebeziumServerSpec implements ConfigMappable {
    private String image;
    private String version;
    private Storage storage;
    private Sink sink;
    private Source source;
    private Format format;
    private Quarkus quarkus;
    private Runtime runtime;
    private List<Transformation> transforms;
    private Map<String, Predicate> predicates;

    public DebeziumServerSpec() {
        this.storage = new Storage();
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

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
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
