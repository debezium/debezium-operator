/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.docs.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Abstract representation of CR reference documentation.
 * Documentation is represented as a collection of documented types see {@link TypeDescription}
 */
public final class Documentation<T> {

    /**
     * Documentation of a type
     *
     * @param name name of this type
     * @param fields field descriptions
     */

    // @formatter:off
    public record TypeDescription<T> (
            String name,
            List<T> fields) {
    }
    // @formatter:on

    /**
     * Builder for {@link TypeDescription}
     * @param <T> field description type
     */
    public static class TypeDescriptionBuilder<T> {
        private String name;
        private final List<T> fields = new ArrayList<>();

        public TypeDescriptionBuilder(String name) {
            this.name = name;
        }

        public TypeDescriptionBuilder<T> addFieldDescription(T field) {
            fields.add(field);
            return this;
        }

        public TypeDescription<T> build() {
            Objects.requireNonNull(name);
            return new TypeDescription<>(name, fields);
        }
    }

    private final String title;
    private final List<TypeDescription<T>> types;
    private final Set<String> typeNames;

    /**
     * Creates new documentation
     * @param title title of this Documentation
     */
    public Documentation(String title) {
        this.title = title;
        this.types = new ArrayList<>();
        this.typeNames = new HashSet<>();
    }

    public Documentation<T> addTypeDescription(TypeDescription<T> type) {
        types.add(type);
        typeNames.add(type.name);
        return this;
    }

    public String title() {
        return title;
    }

    public List<TypeDescription<T>> types() {
        return types;
    }

    public Set<String> typeNames() {
        return typeNames;
    }

    public boolean isKnownType(String name) {
        return typeNames.contains(name);
    }
}
