/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.docs;

import static io.debezium.operator.docs.Processing.annotation;
import static io.debezium.operator.docs.Processing.asDeclared;
import static io.debezium.operator.docs.Processing.asElement;
import static io.debezium.operator.docs.Processing.asEnum;
import static io.debezium.operator.docs.Processing.enclosedElements;
import static io.debezium.operator.docs.Processing.isAnnotated;
import static io.debezium.operator.docs.Processing.typeArguments;
import static java.util.function.Predicate.not;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.StandardLocation;

import com.fasterxml.jackson.annotation.JsonProperty;
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
                .filter(this::isNotHidden)
                .map(this::documentType)
                .forEach(documentation::addTypeDescription);
    }

    protected TypeDescription<TField> documentType(TypeElement element) {
        var name = name(element);
        var description = typeDescriptionBuilder(name);

        presentFields(element)
                .stream()
                .map(this::createFieldDocs)
                .forEach(description::addFieldDescription);

        additionalFields(element)
                .stream()
                .map(this::createFieldDocs)
                .forEach(description::addFieldDescription);

        return description.build();
    }

    private List<Documented.Field> additionalFields(TypeElement element) {
        return documentedTypeInfo(element)
                .stream()
                .map(Documented::fields)
                .flatMap(Stream::of)
                .toList();
    }

    private List<VariableElement> presentFields(TypeElement element) {
        return enclosedElements(element, ElementKind.FIELD, VariableElement.class)
                .filter(this::isDocumentedField)
                .toList();
    }

    private void setKnownTypes(Collection<TypeElement> types) {
        types.stream()
                .filter(this::isNotHidden)
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
     * @return name of the element
     */
    protected String name(Element element) {
        return String.valueOf(element.getSimpleName());
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

    protected Optional<Documented.Field> documentedFieldInfo(Element element) {
        return annotation(element, Documented.Field.class);
    }

    protected Optional<JsonPropertyDescription> jsonPropertyDescription(Element element) {
        return annotation(element, JsonPropertyDescription.class);
    }

    protected Optional<JsonProperty> jsonProperty(Element element) {
        return annotation(element, JsonProperty.class);
    }

    protected Optional<Documented> documentedTypeInfo(TypeMirror type) {
        return asElement(type).flatMap(e -> annotation(e, Documented.class));
    }

    protected Optional<Documented> documentedTypeInfo(TypeElement type) {
        return annotation(type, Documented.class);
    }

    protected boolean isDocumentedField(Element field) {
        return isAnnotated(field, Documented.Field.class)
                || isAnnotated(field, JsonProperty.class)
                || isAnnotated(field, JsonPropertyDescription.class);
    }

    protected boolean isNotHidden(TypeElement element) {
        return documentedTypeInfo(element)
                .map(Documented::hidden)
                .map(hidden -> !hidden)
                .orElse(true);
    }

    protected boolean isKnownType(String name) {
        return knownTypes.contains(name);
    }

    protected String fieldDefaultValue(Element element) {
        return Optional.<String> empty()
                .or(() -> documentedFieldInfo(element)
                        .map(Documented.Field::defaultValue)
                        .filter(not(String::isEmpty)))
                .or(() -> jsonProperty(element)
                        .map(JsonProperty::defaultValue)
                        .filter(not(String::isEmpty)))
                .orElse("");
    }

    /**
     * Gets descriptions from  {@link JsonPropertyDescription}
     *
     * @param element annotated element
     * @return field description
     */
    protected String fieldDescription(Element element) {
        return Optional.<String> empty()
                .or(() -> documentedFieldInfo(element)
                        .map(Documented.Field::description)
                        .filter(not(String::isEmpty)))
                .or(() -> jsonPropertyDescription(element)
                        .map(JsonPropertyDescription::value)
                        .filter(not(String::isEmpty)))
                .orElse("");
    }

    /**
     * @param field variable field
     * @return type with generic arguments
     */
    protected String fieldType(VariableElement field) {
        var type = field.asType();
        return Optional.<String> empty()
                .or(() -> explicitFieldTypeName(field))
                .or(() -> explicitTypeName(type))
                .or(() -> enumTypeName(type))
                .or(() -> declaredTypeName(type))
                .orElseGet(() -> typeName(type));
    }

    /**
     * Extracts type documented by {@link Documented.Field}
     *
     * @param field scanned field
     * @return documented type or empty
     */
    protected Optional<String> explicitFieldTypeName(VariableElement field) {
        return documentedFieldInfo(field)
                .map(Documented.Field::type)
                .filter(not(String::isEmpty));
    }

    protected Optional<String> explicitTypeName(TypeMirror type) {
        return documentedTypeInfo(type)
                .map(Documented::name)
                .filter(not(String::isEmpty));
    }

    protected Optional<String> enumTypeName(TypeMirror type) {
        return asEnum(type)
                .map(this::enumConstantNames)
                .map(names -> String.join(",", names));
    }

    protected Optional<String> declaredTypeName(TypeMirror mirror) {
        return asDeclared(mirror).map(this::genericTypeName);
    }

    protected String typeName(TypeMirror type) {
        var fullName = type.toString();
        return simpleName(fullName);
    }

    protected String genericTypeName(DeclaredType type) {
        var erasure = processingEnv.getTypeUtils().erasure(type);
        var typeArgs = new StringJoiner(", ", "<", ">").setEmptyValue("");

        typeArguments(type)
                .map(this::typeName)
                .forEach(typeArgs::add);

        return typeName(erasure) + typeArgs;
    }

    private List<String> enumConstantNames(TypeElement type) {
        return enclosedElements(type, ElementKind.ENUM_CONSTANT)
                .map(this::name)
                .map(String::toLowerCase)
                .toList();
    }

    /**
     * Extracts a type reference from a field's type.

     * @param field variable element
     * @return name of documented type
     */
    protected String fieldTypeReference(VariableElement field) {
        var type = field.asType();

        return Optional.<String> empty()
                .or(() -> typeErasureReference(type))
                .or(() -> typeArgumentReference(type))
                .orElse(null);
    }

    /**
     * Extracts external type reference URL  from {@link Documented.Field} present on a field
     *
     * @return reference url
     */
    protected String fieldExternalTypeReference(VariableElement field) {
        return documentedFieldInfo(field)
                .map(this::fieldExternalTypeReference)
                .orElse(null);
    }

    protected String fieldExternalTypeReference(Documented.Field field) {
        return Optional.<String> empty()
                .or(() -> k8TypeReference(field.k8Ref()))
                .orElse(null);
    }

    private Optional<String> k8TypeReference(String slug) {
        if (slug.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Documented.K8_API_DOCS_ADDR + "#" + slug);
    }

    protected Optional<String> typeErasureReference(TypeMirror type) {
        var erasure = processingEnv.getTypeUtils().erasure(type);

        return asDeclared(erasure)
                .map(this::typeName)
                .filter(this::isKnownType);
    }

    protected Optional<String> typeArgumentReference(TypeMirror type) {
        return asDeclared(type)
                .map(t -> typeArguments(t, this::typeName))
                .map(s -> s.filter(this::isKnownType))
                .flatMap(Stream::findFirst);
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
    protected abstract TField createFieldDocs(Documented.Field field);

    /**
     * Called for each field documented by {@link JsonPropertyDescription}
     *
     * @param field field as {@link VariableElement}
     * @return instance of {@link TField}
     */
    protected abstract TField createFieldDocs(VariableElement field);
}
