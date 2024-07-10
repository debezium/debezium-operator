/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.docs;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

import com.google.auto.service.AutoService;

import io.debezium.operator.docs.output.AsciidocFormatter;

@SupportedSourceVersion(SourceVersion.RELEASE_21)
@AutoService(Processor.class)
public class DebeziumOperatorDocsProcessor extends AbstractDocsProcessor {
    public static final String DOCS_FILE_NAME = "reference.adoc";
    public static final String DOCS_TITLE = "Debezium Operator Schema Reference";

    public DebeziumOperatorDocsProcessor() {
        super(DOCS_FILE_NAME, DOCS_TITLE);
    }

    @Override
    protected AsciidocFormatter formatter() {
        return new AsciidocFormatter();
    }

}
