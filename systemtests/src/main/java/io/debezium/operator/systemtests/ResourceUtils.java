/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.systemtests;

import java.io.IOException;
import java.net.URL;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

public class ResourceUtils {
    public static <T> T readYaml(URL fileName, Class<T> c) {
        ObjectMapper mapper = new YAMLMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            return mapper.readValue(fileName, c);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
