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
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.auto.service.AutoService;

import io.debezium.operator.docs.annotations.Documented;

@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class DebeziumOperatorDocsProcessor extends AbstractProcessor {

    public static final String DOCS_FILE_NAME = "reference.adoc";
    private final StringBuilder docs = new StringBuilder();

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(Documented.class.getCanonicalName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            var annotated = roundEnv.getElementsAnnotatedWith(annotation);
            var types = ElementFilter.typesIn(annotated);
            documentTypes(types);
            writeDocFile(DOCS_FILE_NAME);
        }

        return false;
    }

    private void writeDocFile(String fileName) {
        try {
            FileObject resource = processingEnv
                    .getFiler()
                    .createResource(StandardLocation.SOURCE_OUTPUT, "docs", fileName);

            try (PrintWriter out = new PrintWriter(resource.openWriter())) {
                out.println(docs);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void documentTypes(Collection<TypeElement> types) {
        docs.append("""
                [[debezium-operator-schema-reference]]
                === Debezium Operator Schema Reference properties
                """).append("\n");

        types.forEach(this::documentType);

    }

    private void documentType(TypeElement element) {
        var name = typeName(element);
        var fields = describedFields(element);
        var explicitFields = explicitFields(element);

        var header = """
                [id="debezium-operator-$id-schema-reference"]
                .$name schema reference
                [cols="20%a,20%s,20%a,40%a",options="header"]
                |===
                |Property |Type |Default |Description
                """
                .replace("$id", name.toLowerCase())
                .replace("$name", name);
        var footer = "|===";

        docs.append(header).append("\n");
        documentExplicitFields(explicitFields);
        documentFields(fields);
        docs.append(footer).append("\n\n");
    }

    private void documentExplicitFields(Collection<Documented.Field> fields) {
        fields.forEach(this::documentExplicitField);
    }

    private void documentFields(Collection<VariableElement> fields) {
        fields.forEach(this::documentField);
    }

    private void documentExplicitField(Documented.Field field) {
        documentAnyField(field.name(), field.type(), field.defaultVal(), field.description());
    }

    private void documentField(VariableElement field) {
        documentAnyField(fieldName(field), fieldType(field), "", fieldDescription(field));
    }

    private void documentAnyField(String name, String type, String defaultVal, String description) {
        docs.append("|").append(name);
        docs.append(" |").append(type);
        docs.append(" |").append(defaultVal);
        docs.append(" |").append(description);
        docs.append("\n");
    }

    private List<Documented.Field> explicitFields(TypeElement element) {
        var fields = element.getAnnotation(Documented.class);
        return Arrays.asList(fields.fields());
    }

    private List<VariableElement> describedFields(TypeElement element) {
        return element.getEnclosedElements()
                .stream()
                .filter(e -> e.getKind() == ElementKind.FIELD)
                .map(VariableElement.class::cast)
                .filter(this::hasDescription)
                .toList();
    }

    private <A extends Annotation> boolean isAnnotated(VariableElement field, Class<A> annotationType) {
        return field.getAnnotation(annotationType) != null;
    }

    private boolean hasDescription(VariableElement field) {
        return isAnnotated(field, JsonPropertyDescription.class);
    }

    private String typeName(TypeElement element) {
        var fullName = String.valueOf(element.getSimpleName());
        return simpleName(fullName);
    }

    private String typeName(TypeMirror type) {
        var fullName = type.toString();
        return simpleName(fullName);
    }

    private String simpleName(String fullName) {
        var dot = fullName.lastIndexOf('.') + 1;
        return fullName.substring(dot);
    }

    private String fieldName(VariableElement field) {
        return String.valueOf(field.getSimpleName());
    }

    private String fieldType(VariableElement field) {
        var type = field.asType();

        if (field.asType() instanceof DeclaredType declared) {
            var erasure = processingEnv.getTypeUtils().erasure(declared);
            var typeArgs = new StringJoiner(", ", "<", ">").setEmptyValue("");

            declared.getTypeArguments()
                    .stream()
                    .map(this::typeName)
                    .forEach(typeArgs::add);

            return typeName(erasure) + typeArgs;
        }

        return typeName(type);
    }

    private String fieldDescription(VariableElement field) {
        var annotation = field.getAnnotation(JsonPropertyDescription.class);
        return annotation.value();
    }
}
