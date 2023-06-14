/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.debezium.operator.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ConfigMappingTest {

    @Test
    void shouldNotAddNullValue() {
        var config = ConfigMapping.empty();
        config.put("invalid", null);

        assertThat(config.getAsMap()).isEmpty();
        assertThat(config.getAsString()).isEmpty();
    }
}
