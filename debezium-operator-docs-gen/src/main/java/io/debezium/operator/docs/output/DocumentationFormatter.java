/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.docs.output;

import io.debezium.operator.docs.model.Documentation;

public interface DocumentationFormatter {
    String formatted(Documentation documentation);
}
