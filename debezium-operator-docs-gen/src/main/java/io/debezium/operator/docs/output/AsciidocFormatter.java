/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.docs.output;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.debezium.operator.docs.model.Documentation;
import io.debezium.operator.docs.model.Documentation.TypeDescription;
import io.debezium.operator.docs.model.FieldDescription;

public final class AsciidocFormatter implements DocumentationFormatter<FieldDescription> {

    private String identifier(String... names) {
        return Stream.of(names)
                .map(String::toLowerCase)
                .map(name -> name.replace(" ", "-"))
                .collect(Collectors.joining("-"));

    }

    private String formatDocHeader(Documentation<FieldDescription> documentation) {
        var template = """
                [#%s]
                === %s

                """;

        return template.formatted(
                identifier(documentation.title()),
                documentation.title());
    }

    private String formatType(Documentation<FieldDescription> documentation, TypeDescription<FieldDescription> type) {
        var template = """
                [#%s]
                .%s schema reference
                [cols="20%%a,25%%s,15%%a,40%%a",options="header"]
                |===
                | Property | Type | Default | Description
                %s
                |===

                """;

        return template.formatted(
                identifier(documentation.title(), type.name()),
                type.name(),
                formatFields(documentation, type));
    }

    private String formatField(Documentation<FieldDescription> documentation, TypeDescription<FieldDescription> type, FieldDescription field) {
        var template = "| %s | %s | %s | %s";

        return template.formatted(
                formatFieldName(documentation, type, field),
                formatFieldType(documentation, type, field),
                field.defaultValue(),
                field.description());
    }

    private String formatFieldName(Documentation<FieldDescription> documentation, TypeDescription<FieldDescription> type, FieldDescription field) {
        var template = "[[%s]]<<%s, `+%s+`>>";

        return template.formatted(
                identifier(documentation.title(), type.name(), field.name()),
                identifier(documentation.title(), type.name(), field.name()),
                field.name());
    }

    private String formatFieldType(Documentation<FieldDescription> documentation, TypeDescription<FieldDescription> type, FieldDescription field) {
        if (field.typeRef() != null) {
            var template = "<<%s, `+%s+`>>";

            return template.formatted(
                    identifier(documentation.title(), field.typeRef()),
                    field.type());
        }

        if (field.externalTypeRef() != null) {
            var template = "%s[`+%s+`]";

            return template.formatted(
                    field.externalTypeRef(),
                    field.type());
        }

        return field.type();
    }

    private String formatFields(Documentation<FieldDescription> documentation, TypeDescription<FieldDescription> type) {
        return type.fields()
                .stream()
                .map(f -> formatField(documentation, type, f))
                .collect(Collectors.joining("\n"));
    }

    @Override
    public String formatted(Documentation<FieldDescription> documentation) {
        var docs = new StringBuilder();

        docs.append(formatDocHeader(documentation));

        documentation.types()
                .stream()
                .map(t -> formatType(documentation, t))
                .forEach(docs::append);

        return docs.toString();
    }
}
