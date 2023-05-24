/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.model;

import java.util.List;

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
    private ExternalConfiguration externalConfiguration;
    private List<Transformation> transforms;
    private List<Predicate> predicates;

    public DebeziumServerSpec() {
        this.storage = new Storage();
        this.sink = new Sink();
        this.source = new Source();
        this.format = new Format();
        this.quarkus = new Quarkus();
        this.externalConfiguration = new ExternalConfiguration();
        this.transforms = List.of();
        this.predicates = List.of();
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

    public ExternalConfiguration getExternalConfiguration() {
        return externalConfiguration;
    }

    public void setExternalConfiguration(ExternalConfiguration externalConfiguration) {
        this.externalConfiguration = externalConfiguration;
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

    public List<Predicate> getPredicates() {
        return predicates;
    }

    public void setPredicates(List<Predicate> predicates) {
        this.predicates = predicates;
    }

    @Override
    public ConfigMapping asConfiguration() {
        var dbzConfig = ConfigMapping.prefixed("debezium");
        dbzConfig.put("source", source);
        dbzConfig.put("sink", sink);
        dbzConfig.put("format", format);
        dbzConfig.put("transforms", transforms, Transformation::getName);
        dbzConfig.put("predicates", predicates, Predicate::getName);

        var config = ConfigMapping.empty();
        config.put("quarkus", quarkus);
        config.put(dbzConfig.getAsMap());

        return config;
    }
}
