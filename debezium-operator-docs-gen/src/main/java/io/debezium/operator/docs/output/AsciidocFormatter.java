/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.docs.output;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.debezium.operator.docs.model.Documentation;
import io.debezium.operator.docs.model.Documentation.TypeDescription;

public final class AsciidocFormatter implements DocumentationFormatter {

    private String identifier(String... names) {
        return Stream.of(names)
                .map(String::toLowerCase)
                .map(name -> name.replace(" ", "-"))
                .collect(Collectors.joining("-"));

    }

    private String formatDocHeader(Documentation documentation) {
        var template = """
                [#%s]
                === %s

                """;

        return template.formatted(
                identifier(documentation.title()),
                documentation.title());
    }

    private String formatType(Documentation documentation, TypeDescription type) {
        var template = """
                [#%s]
                ==== %s Schema Reference
                %s

                .%s properties
                [cols="20%%a,25%%s,15%%a,40%%a",options="header"]
                |===
                | Property | Type | Default | Description
                %s
                |===

                """;

        return template.formatted(
                identifier(documentation.title(), type.name()),
                type.name(),
                formatUsageReference(documentation, type),
                type.name(),
                formatFields(documentation, type));
    }

    private String formatUsageReference(Documentation documentation, TypeDescription type) {
        var template = "Used in: %s\n";
        var usages = documentation.getUsages(type.name())
                .stream()
                .sorted()
                .map(name -> formatTypeReference(documentation, name, name))
                .collect(Collectors.joining(", "));

        if (usages.isEmpty()) {
            return "";
        }

        return template.formatted(usages);
    }

    private String formatField(Documentation documentation, TypeDescription type, Documentation.FieldDescription field) {
        var template = "| %s | %s | %s | %s";

        return template.formatted(
                formatFieldName(documentation, type, field),
                formatFieldType(documentation, type, field),
                field.defaultValue(),
                field.description());
    }

    private String formatFieldName(Documentation documentation, TypeDescription type, Documentation.FieldDescription field) {
        var template = "[[%s]]<<%s, `+%s+`>>";

        return template.formatted(
                identifier(documentation.title(), type.name(), field.name()),
                identifier(documentation.title(), type.name(), field.name()),
                field.name());
    }

    private String formatTypeReference(Documentation documentation, String type, String typeRef) {
        var template = "<<%s, `+%s+`>>";

        return template.formatted(
                identifier(documentation.title(), typeRef),
                type);
    }

    private String formatFieldType(Documentation documentation, TypeDescription type, Documentation.FieldDescription field) {
        if (field.typeRef() != null) {
            return formatTypeReference(documentation, field.type(), field.typeRef());
        }

        if (field.externalTypeRef() != null) {
            var template = "%s[`+%s+`]";

            return template.formatted(
                    field.externalTypeRef(),
                    field.type());
        }

        return field.type();
    }

    private String formatFields(Documentation documentation, TypeDescription type) {
        return type.fields()
                .stream()
                .map(f -> formatField(documentation, type, f))
                .collect(Collectors.joining("\n"));
    }

    private Map<String, Set<String>> index = new HashMap<>();

    @Override
    public String formatted(Documentation documentation) {
        var docs = new StringBuilder();

        docs.append(formatDocHeader(documentation));

        documentation.types()
                .stream()
                .sorted(Comparator.comparing(TypeDescription::name))
                .map(t -> formatType(documentation, t))
                .forEach(docs::append);

        return docs.toString();
    }
}
