/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.systemtests.resources.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

import io.debezium.operator.systemtests.resources.annotations.extensions.DebeziumResourceTypesExtension;

@Target(ElementType.TYPE)
@Retention(RUNTIME)
@ExtendWith(DebeziumResourceTypesExtension.class)
public @interface DebeziumResourceTypes {
}
