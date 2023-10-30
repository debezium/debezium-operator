/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.debezium.operator.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.debezium.operator.model.ConfigProperties;
import io.debezium.operator.model.Predicate;
import io.debezium.operator.model.Transformation;

public class ConfigMappingTest {

    @Test
    void shouldNotAddNullValue() {
        var config = ConfigMapping.empty();
        config.put("invalid", null);

        assertThat(config.getAsMap()).isEmpty();
        assertThat(config.getAsString()).isEmpty();
    }

    @Test
    void shouldAddRootValue() {
        var config = ConfigMapping.empty();
        config.rootValue("json");

        assertThat(config.getAsMap()).containsEntry("", "json");
        assertThat(config.getAsString()).isEqualTo("=json");
    }

    @Test
    void shouldAddKeyAndValue() {
        var config = ConfigMapping.empty();
        config.put("key", "value");

        assertThat(config.getAsMap()).containsEntry("key", "value");
        assertThat(config.getAsString()).isEqualTo("key=value");
    }

    @Test
    void shouldAddValuesFromMap() {
        Map<String, String> properties = new HashMap<>();
        properties.put("key1", "value1");
        properties.put("key2", "value2");

        var config = ConfigMapping.empty();
        config.putAll(properties);

        assertThat(config.getAsMap()).contains(entry("key1", "value1"), entry("key2", "value2"));
        assertThat(config.getAsString()).isEqualTo("key1=value1\nkey2=value2");
    }

    @Test
    void shouldAddList() {
        var config = ConfigMapping.empty();

        List<Map<String, Object>> configDataList = List.of(
                Map.of("type", "io.debezium.transforms.ByLogicalTableRouter", "negate", false),
                Map.of("type", "io.debezium.transforms.ByLogicalTableRouter", "negate", true));

        List<Transformation> transformations = new ArrayList<>();

        for (Map<String, Object> configData : configDataList) {
            ConfigProperties configProperties = new ConfigProperties();
            Transformation transformation = new Transformation();
            transformation.setType((String) configData.get("type"));
            transformation.setNegate((Boolean) configData.get("negate"));
            transformation.setConfig(configProperties);
            transformations.add(transformation);
        }

        config.putList("transforms", transformations, "Reroute");

        assertThat(config.getAsMap()).containsEntry("transforms", "");
        assertThat(config.getAsMap()).containsEntry("transforms.Reroute0.type", "io.debezium.transforms.ByLogicalTableRouter");
        assertThat(config.getAsMap()).containsEntry("transforms.Reroute0.negate", "false");
        assertThat(config.getAsMap()).containsEntry("transforms.Reroute1.type", "io.debezium.transforms.ByLogicalTableRouter");
        assertThat(config.getAsMap()).containsEntry("transforms.Reroute1.negate", "true");

        assertThat(config.getAsString()).isEqualTo("transforms.Reroute0.negate=false\n" +
                "transforms.Reroute0.type=io.debezium.transforms.ByLogicalTableRouter\n" +
                "transforms.Reroute1.negate=true\n" +
                "transforms.Reroute1.type=io.debezium.transforms.ByLogicalTableRouter\n" +
                "transforms=");
    }

    @Test
    void shouldAddMap() {
        var config = ConfigMapping.empty();
        ConfigProperties configProperties = new ConfigProperties();
        Predicate predicate = new Predicate();
        predicate.setType("IsOutboxTable");
        predicate.setConfig(configProperties);

        Map<String, Predicate> predicateMap = Map.of("predicates", predicate);
        config.putMap("map", predicateMap);

        assertThat(config.getAsMap()).containsEntry("map", "predicates");
        assertThat(config.getAsMap()).containsEntry("map.predicates.type", "IsOutboxTable");
        assertThat(config.getAsString()).isEqualTo("map.predicates.type=IsOutboxTable\n" +
                "map=predicates");
    }

    @Test
    void shouldCalculateMD5Sum() {
        var config = ConfigMapping.empty();
        config.put("key1", "value1");
        String md5Sum = config.md5Sum();

        assertThat(md5Sum).isEqualTo("9767c4972ba72e87ab553bad2afde741");
    }
}
