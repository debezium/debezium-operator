/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.docs;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.VariableElement;

import com.google.auto.service.AutoService;

import io.debezium.operator.docs.annotations.Documented;
import io.debezium.operator.docs.model.FieldDescription;
import io.debezium.operator.docs.output.AsciidocFormatter;

@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class DebeziumOperatorDocsProcessor
        extends AbstractDocsProcessor<FieldDescription> {
    public static final String DOCS_FILE_NAME = "reference.adoc";
    public static final String DOCS_TITLE = "Debezium Operator Schema Reference";

    public DebeziumOperatorDocsProcessor() {
        super(DOCS_FILE_NAME, DOCS_TITLE);
    }

    @Override
    protected AsciidocFormatter formatter() {
        return new AsciidocFormatter();
    }

    protected FieldDescription fieldDescription(Documented.Field field) {
        return new FieldDescription(field.name(), field.type(), field.type(), field.defaultVal(), field.description());
    }

    protected FieldDescription fieldDescription(VariableElement field) {
        return new FieldDescription(name(field), type(field), typeReference(field), "", descriptionValue(field));
    }
}
