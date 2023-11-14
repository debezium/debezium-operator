/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.commons.util;

import static io.debezium.operator.commons.util.StringUtils.joinAsJavaOpts;
import static io.debezium.operator.commons.util.StringUtils.splitJavaOpts;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

public class StringUtilTest {

    static final String EMPTY_PROP_STRING = "";
    static final String SINGLE_PROP_STRING = "-Dprop=value";
    static final String MULTI_PROP_STRING = "-Dprop=value -Xmx128M";
    static final Map<String, String> EMPTY_PROP_MAP = Map.of();
    static final Map<String, String> SINGLE_PROP_MAP = Map.of("-Dprop", "value");
    static final Map<String, String> MULTI_PROP_MAP = Map.of(
            "-Dprop", "value",
            "-Xmx128M", "");

    @Test
    void shouldJoinMapAsJavaOpts() {
        assertThat(joinAsJavaOpts(EMPTY_PROP_MAP))
                .isEqualTo(EMPTY_PROP_STRING);
        assertThat(joinAsJavaOpts(SINGLE_PROP_MAP))
                .isEqualTo(SINGLE_PROP_STRING);
        assertThat(joinAsJavaOpts(MULTI_PROP_MAP))
                .hasSameSizeAs(MULTI_PROP_STRING)
                .contains(MULTI_PROP_MAP.keySet())
                .contains(MULTI_PROP_MAP.values());
    }

    @Test
    void shouldSplitPropertiesIntoMap() {
        assertThat(splitJavaOpts(EMPTY_PROP_STRING))
                .containsExactlyInAnyOrderEntriesOf(EMPTY_PROP_MAP);
        assertThat(splitJavaOpts(SINGLE_PROP_STRING))
                .containsExactlyInAnyOrderEntriesOf(SINGLE_PROP_MAP);
        assertThat(splitJavaOpts(MULTI_PROP_STRING))
                .containsExactlyInAnyOrderEntriesOf(MULTI_PROP_MAP);

    }
}
