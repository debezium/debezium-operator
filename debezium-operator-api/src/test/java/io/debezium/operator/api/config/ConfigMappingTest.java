/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.debezium.operator.api.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.debezium.operator.api.model.ConfigProperties;
import io.debezium.operator.api.model.Predicate;
import io.debezium.operator.api.model.Transformation;

public class ConfigMappingTest {

    @Test
    void shouldNotAddNullValue() {
        var config = ConfigMapping.empty();
        config.put("invalid", null);

        assertThat(config.getAsMapSimple()).isEmpty();
        assertThat(config.getAsString()).isEmpty();
    }

    @Test
    void shouldAddRootValue() {
        var config = ConfigMapping.empty();
        config.rootValue("json");

        assertThat(config.getAsMapSimple()).containsEntry("", "json");
        assertThat(config.getAsString()).isEqualTo("=json");
    }

    @Test
    void shouldAddKeyAndValue() {
        var config = ConfigMapping.empty();
        config.put("name", "value");

        assertThat(config.getAsMapSimple()).containsEntry("name", "value");
        assertThat(config.getAsString()).isEqualTo("name=value");
    }

    @Test
    void shouldAddKeyAndValueWithPrefix() {
        var config = ConfigMapping.prefixed("prefix");
        config.put("name", "value");

        assertThat(config.getAsMapSimple()).containsEntry("prefix.name", "value");
        assertThat(config.getAsString()).isEqualTo("prefix.name=value");
    }

    @Test
    void shouldAddKeyAndValueWithAbsKeyWithPrefix() {
        var config = ConfigMapping.prefixed("prefix");
        config.putAbs("name", "value");

        assertThat(config.getAsMapSimple()).containsEntry("name", "value");
        assertThat(config.getAsString()).isEqualTo("name=value");
    }

    @Test
    void shouldAddChildConfigDirectly() {
        var childConfig = ConfigMapping.empty();
        childConfig.put("childName", "childValue");
        var parent = ConfigMapping.empty();
        parent.put("name", "value");
        parent.putAll(childConfig);

        assertThat(parent.getAsMapSimple())
                .contains(entry("name", "value"), entry("childName", "childValue"));
        assertThat(parent.getAsString()).isEqualTo("childName=childValue\nname=value");
    }

    @Test
    void shouldAddChildConfigWithPrefixDirectly() {
        var childConfig = ConfigMapping.prefixed("prefix");
        childConfig.put("childName", "childValue");
        var parent = ConfigMapping.empty();
        parent.put("name", "value");
        parent.putAll(childConfig);

        assertThat(parent.getAsMapSimple())
                .contains(entry("name", "value"), entry("prefix.childName", "childValue"));
        assertThat(parent.getAsString()).isEqualTo("name=value\nprefix.childName=childValue");
    }

    @Test
    void shouldAddChildConfigWithKey() {
        var childConfig = ConfigMapping.empty();
        childConfig.put("name", "value");
        var parent = ConfigMapping.empty();
        parent.put("name", "value");
        parent.putAll("child", childConfig);

        assertThat(parent.getAsMapSimple())
                .contains(entry("name", "value"), entry("child.name", "value"));
        assertThat(parent.getAsString()).isEqualTo("child.name=value\nname=value");
    }

    @Test
    void shouldAddChildConfigWithPrefixWithKey() {
        var childConfig = ConfigMapping.prefixed("prefix");
        childConfig.put("name", "value");
        var parent = ConfigMapping.empty();
        parent.put("name", "value");
        parent.putAll("child", childConfig);

        assertThat(parent.getAsMapSimple())
                .contains(entry("name", "value"), entry("child.prefix.name", "value"));
        assertThat(parent.getAsString()).isEqualTo("child.prefix.name=value\nname=value");
    }

    @Test
    void shouldAddChildConfigWithAbsoluteKeyWithKey() {
        var childConfig = ConfigMapping.empty();
        childConfig.put("name", "value");
        childConfig.putAbs("name2", "value2");
        var parent = ConfigMapping.empty();
        parent.put("name", "value");
        parent.putAll("child", childConfig);

        assertThat(parent.getAsMapSimple())
                .contains(entry("name", "value"), entry("child.name", "value"), entry("name2", "value2"));
        assertThat(parent.getAsString()).isEqualTo("child.name=value\nname2=value2\nname=value");
    }

    @Test
    void shouldAddValuesFromMap() {
        Map<String, String> properties = new HashMap<>();
        properties.put("key1", "value1");
        properties.put("key2", "value2");

        var config = ConfigMapping.empty();
        config.putAll(properties);

        assertThat(config.getAsMapSimple()).contains(entry("key1", "value1"), entry("key2", "value2"));
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

        assertThat(config.getAsMapSimple()).containsEntry("transforms", "Reroute0,Reroute1");
        assertThat(config.getAsMapSimple()).containsEntry("transforms.Reroute0.type", "io.debezium.transforms.ByLogicalTableRouter");
        assertThat(config.getAsMapSimple()).containsEntry("transforms.Reroute0.negate", "false");
        assertThat(config.getAsMapSimple()).containsEntry("transforms.Reroute1.type", "io.debezium.transforms.ByLogicalTableRouter");
        assertThat(config.getAsMapSimple()).containsEntry("transforms.Reroute1.negate", "true");
        assertThat(config.getAsString()).isEqualTo("transforms.Reroute0.negate=false\n" +
                "transforms.Reroute0.type=io.debezium.transforms.ByLogicalTableRouter\n" +
                "transforms.Reroute1.negate=true\n" +
                "transforms.Reroute1.type=io.debezium.transforms.ByLogicalTableRouter\n" +
                "transforms=Reroute0,Reroute1");
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

        assertThat(config.getAsMapSimple()).containsEntry("map", "predicates");
        assertThat(config.getAsMapSimple()).containsEntry("map.predicates.type", "IsOutboxTable");
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
