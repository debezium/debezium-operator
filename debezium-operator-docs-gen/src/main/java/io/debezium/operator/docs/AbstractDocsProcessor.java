/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.docs;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.StandardLocation;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.debezium.operator.docs.annotations.Documented;
import io.debezium.operator.docs.model.Documentation;
import io.debezium.operator.docs.model.Documentation.TypeDescription;
import io.debezium.operator.docs.model.Documentation.TypeDescriptionBuilder;
import io.debezium.operator.docs.output.DocumentationFormatter;

public abstract class AbstractDocsProcessor<TField> extends AbstractProcessor {
    private final Documentation<TField> documentation;
    private final String file;
    private final Set<String> knownTypes;

    public AbstractDocsProcessor(String file, String title) {
        this.documentation = new Documentation<>(title);
        this.knownTypes = new HashSet<>();
        this.file = file;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(Documented.class.getCanonicalName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            var annotated = roundEnv.getElementsAnnotatedWith(annotation);
            var types = ElementFilter.typesIn(annotated);

            setKnownTypes(types);
            documentTypes(types);
            writeDocFile(file);
        }
        return false;
    }

    protected void writeDocFile(String fileName) {
        try {
            var content = formatter()
                    .formatted(documentation);
            var resource = processingEnv
                    .getFiler()
                    .createResource(StandardLocation.SOURCE_OUTPUT, "docs", fileName);

            try (PrintWriter out = new PrintWriter(resource.openWriter())) {
                out.println(content);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void documentTypes(Collection<TypeElement> types) {
        types
                .stream()
                .map(this::documentType)
                .forEach(documentation::addTypeDescription);
    }

    protected TypeDescription<TField> documentType(TypeElement element) {
        var name = name(element);
        var description = typeDescriptionBuilder(name);

        presentFields(element)
                .stream()
                .map(this::fieldDescription)
                .forEach(description::addFieldDescription);

        additionalFields(element)
                .stream()
                .map(this::fieldDescription)
                .forEach(description::addFieldDescription);

        return description.build();
    }

    private List<Documented.Field> additionalFields(TypeElement element) {
        var fields = element.getAnnotation(Documented.class);
        return Arrays.asList(fields.fields());
    }

    private List<VariableElement> presentFields(TypeElement element) {
        return element.getEnclosedElements()
                .stream()
                .filter(e -> e.getKind() == ElementKind.FIELD)
                .map(VariableElement.class::cast)
                .filter(this::hasDescription)
                .toList();
    }

    private void setKnownTypes(Set<TypeElement> types) {
        types
                .stream()
                .map(this::name)
                .forEach(knownTypes::add);
    }

    /**
     * @param type type given as {@link TypeElement} instance
     * @return name of the type
     */
    protected String name(TypeElement type) {
        var fullName = String.valueOf(type.getSimpleName());
        return simpleName(fullName);
    }

    /**
     * @param type type given as {@link TypeMirror} instance
     * @return name of the type
     */
    protected String name(TypeMirror type) {
        var fullName = type.toString();
        return simpleName(fullName);
    }

    /**
     * @param field  given as {@link VariableElement} instance
     * @return name of the field
     */
    protected String name(VariableElement field) {
        return String.valueOf(field.getSimpleName());
    }

    /**
     * For a fully qualified name, this method returns the part after the last dot
     *
     * @param fullName fully qualified name
     * @return simple name
     */
    private String simpleName(String fullName) {
        var dot = fullName.lastIndexOf('.') + 1;
        return fullName.substring(dot);
    }

    /**
     * Gets field descriptions from  {@link JsonPropertyDescription}
     *
     * @param field field given as {@link VariableElement} instance
     * @return field description
     */
    protected String descriptionValue(VariableElement field) {
        var annotation = field.getAnnotation(JsonPropertyDescription.class);
        return annotation.value();
    }

    protected <A extends Annotation> boolean isAnnotated(VariableElement field, Class<A> annotationType) {
        return field.getAnnotation(annotationType) != null;
    }

    protected boolean hasDescription(VariableElement field) {
        return isAnnotated(field, JsonPropertyDescription.class);
    }

    /**
     * @param field variable field
     * @return type with generic arguments
     */
    protected String type(VariableElement field) {
        var type = field.asType();

        if (type instanceof DeclaredType declared) {
            var erasure = processingEnv.getTypeUtils().erasure(declared);
            var typeArgs = new StringJoiner(", ", "<", ">").setEmptyValue("");

            declared.getTypeArguments()
                    .stream()
                    .map(this::name)
                    .forEach(typeArgs::add);

            return name(erasure) + typeArgs;
        }

        return name(type);
    }

    /**
     * Extracts a type reference from a field's type.
     * Type reference is a name of type annotated by the {@link Documented} annotation
     * It can be either the type of the field directly or part of type arguments (e.g. in case of List or Map)
     *
     * @param field variable element
     * @return name of documented type
     */
    protected String typeReference(VariableElement field) {
        var mirror = field.asType();
        var type = Optional.<String> empty();

        if (mirror instanceof DeclaredType declared) {
            type = declared.getTypeArguments()
                    .stream()
                    .map(this::name)
                    .findFirst();
        }

        return type.or(() -> Optional.of(mirror).map(this::name))
                .filter(knownTypes::contains)
                .orElse(null);
    }

    /**
     * Creates an appropriate instance of type description builder
     *
     * @param name type name
     * @return type description builder
     */
    protected TypeDescriptionBuilder<TField> typeDescriptionBuilder(String name) {
        return new TypeDescriptionBuilder<>(name);
    }

    /**
     * Creates an appropriate instance of documentation formatter
     * @return documentation formatter
     */
    protected abstract DocumentationFormatter<TField> formatter();

    /**
     * Called for each field documented by {@link Documented.Field}
     *
     * @param field field as {@link Documented.Field}
     * @return instance of {@link TField}
     */
    protected abstract TField fieldDescription(Documented.Field field);

    /**
     * Called for each field documented by {@link JsonPropertyDescription}
     *
     * @param field field as {@link VariableElement}
     * @return instance of {@link TField}
     */
    protected abstract TField fieldDescription(VariableElement field);
}
