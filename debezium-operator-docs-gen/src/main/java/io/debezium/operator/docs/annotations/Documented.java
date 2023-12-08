/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.docs.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Documented {

    String K8_API_DOCS_ADDR = "https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.28/";

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.SOURCE)
    @interface Field {
        String name() default "";

        String type() default "";

        String k8Ref() default "";

        String defaultVal() default "";

        String description() default "";
    }

    Field[] fields() default {};

    boolean hidden() default false;

    String name() default "";
}
