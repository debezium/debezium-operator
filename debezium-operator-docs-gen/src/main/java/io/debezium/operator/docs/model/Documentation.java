/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.docs.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Abstract representation of CR reference documentation.
 * Documentation is represented as a collection of documented types see {@link TypeDescription}
 */
public final class Documentation {

    /**
     * Documentation of a type
     *
     * @param name name of this type
     * @param fields field descriptions
     */

    // @formatter:off
    public record TypeDescription (
            String name,
            List<FieldDescription> fields) {
    }

    public record FieldDescription(
            String name,
            String type,
            String typeRef,
            String externalTypeRef,
            String defaultValue,
            String description) {
    }
    // @formatter:on

    /**
     * Builder for {@link TypeDescription}
     */
    public static class TypeDescriptionBuilder {
        private final String name;
        private final List<FieldDescription> fields = new ArrayList<>();

        public TypeDescriptionBuilder(String name) {
            this.name = name;
        }

        public TypeDescriptionBuilder addFieldDescription(FieldDescription field) {
            fields.add(field);
            return this;
        }

        public TypeDescription build() {
            Objects.requireNonNull(name);
            return new TypeDescription(name, fields);
        }
    }

    private final String title;
    private final List<TypeDescription> types;
    private final Set<String> typeNames;

    private Map<String, Set<String>> usageReference = new HashMap<>();

    /**
     * Creates new documentation
     * @param title title of this Documentation
     */
    public Documentation(String title) {
        this.title = title;
        this.types = new ArrayList<>();
        this.typeNames = new HashSet<>();
    }

    public Documentation addTypeDescription(TypeDescription type) {
        types.add(type);
        typeNames.add(type.name);

        type.fields().forEach(field -> usageReference
                .computeIfAbsent(field.typeRef(), k -> new HashSet<>())
                .add(type.name()));

        return this;
    }

    public String title() {
        return title;
    }

    public List<TypeDescription> types() {
        return types;
    }

    public Set<String> typeNames() {
        return typeNames;
    }

    public boolean isKnownType(String name) {
        return typeNames.contains(name);
    }

    public Set<String> getUsages(String name) {
        return usageReference.getOrDefault(name, Set.of());
    }

}
