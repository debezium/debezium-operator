/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.docs;

import static java.util.function.Predicate.not;

import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeMirror;

import io.debezium.operator.docs.annotations.Documented;

public final class Processing {

    /**
     * @param element  potentially annotated element
     * @param annotation annotation class
     * @return ture if element is annotated, false otherwise
     */
    public static <A extends Annotation> boolean isAnnotated(Element element, Class<A> annotation) {
        return element.getAnnotation(annotation) != null;
    }

    /**
     * @param element potentially annotated element
     * @param annotation annotation class
     * @return annotation instance or empty optional if not present
     */
    public static <T extends Annotation> Optional<T> annotation(Element element, Class<T> annotation) {
        return Optional.ofNullable(element.getAnnotation(annotation));
    }

    /**
     * Returns type mirror as instance of {@link DeclaredType} when possible
     *
     * @param type type mirror instance
     * @return type as {@link DeclaredType} or empty
     */
    public static Optional<DeclaredType> asDeclared(TypeMirror type) {
        return Optional.of(type)
                .filter(t -> t instanceof DeclaredType)
                .map(DeclaredType.class::cast);
    }

    /**
     * Returns type mirror as instance of {@link Element} if {@code type} is instance of {@link DeclaredType}
     *
     * @param type type mirror instance
     * @return type as {@link Element} or empty
     */
    public static Optional<Element> asElement(TypeMirror type) {
        return asDeclared(type).map(DeclaredType::asElement);
    }

    /**
     * Return type mirror as {@link TypeElement} if it represents specified kind
     *
     * @param type type mirror instance
     * @param kind element kind
     * @return type mirror as {@link TypeElement} or empty
     */
    public static Optional<TypeElement> asTypeElement(TypeMirror type, ElementKind kind) {
        return asElement(type)
                .filter(e -> e.getKind() == kind)
                .map(TypeElement.class::cast);
    }

    /**
     * Return type mirror as {@link TypeElement} if it represents an Enum type
     *
     * @param type type mirror instance
     * @return type mirror as {@link TypeElement} or empty
     */
    public static Optional<TypeElement> asEnum(TypeMirror type) {
        return asTypeElement(type, ElementKind.ENUM);
    }

    /**
     * Filters enclosed elements by kind
     *
     * @param element parent element
     * @return stream of enclosed elements cast to given type
     */
    public static <T extends Element> Stream<T> enclosedElements(TypeElement element, ElementKind kind, Class<T> clazz) {
        return enclosedElements(element, kind).map(clazz::cast);
    }

    /**
     * Filters enclosed elements by kind
     *
     * @param element parent element
     * @return stream of enclosed elements
     */
    public static Stream<? extends Element> enclosedElements(TypeElement element, ElementKind kind) {
        return enclosedElements(element).filter(e -> e.getKind() == kind);
    }

    /**
     * @param element parent element
     * @return stream of enclosed elements
     */
    public static Stream<? extends Element> enclosedElements(TypeElement element) {
        var parents = annotation(element, Documented.class)
                .map(Documented::parent)
                .orElse(false);

        var allEnclosed = new LinkedList<Element>();
        var current = element;
        do {
            var enclosed = current.getEnclosedElements();
            allEnclosed.addAll(enclosed);
            current = parent(current);
        } while (parents && current != null);

        return allEnclosed.stream();
    }

    /**
     * Returns parent class of given element
     * @param element the type element
     * @return parent or null in case that the parent is {@link java.lang.Object}
     */
    public static TypeElement parent(TypeElement element) {
        var parent = element.getSuperclass();
        return Optional.of(parent)
                .filter(not(p -> p instanceof NoType))
                .flatMap(p -> asTypeElement(p, ElementKind.CLASS))
                .orElse(null);
    }

    public static Stream<? extends TypeMirror> typeArguments(DeclaredType type) {
        return type.getTypeArguments().stream();
    }

    public static <T> Stream<T> typeArguments(DeclaredType type, Function<TypeMirror, T> mapper) {
        return typeArguments(type).map(mapper);
    }

    private Processing() {
        // intentionally private
    }
}
