/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.config;

import static java.util.function.Predicate.not;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Convenience wrapper used to build properties-like configuration from arbitrary map
 */
public final class ConfigMapping {

    private final Map<String, String> config;
    private final String prefix;

    public static ConfigMapping from(Map<String, ?> properties) {
        var config = ConfigMapping.empty();
        config.putAll(properties);
        return config;
    }

    public static ConfigMapping empty() {
        return new ConfigMapping(null);
    }

    public static ConfigMapping prefixed(String prefix) {
        return new ConfigMapping(prefix);
    }

    public ConfigMapping(String prefix) {
        this.config = new HashMap<>();
        this.prefix = prefix;
    }

    public Map<String, String> getAsMap() {
        return config;
    }

    public String getAsString() {
        return config.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .sorted()
                .collect(Collectors.joining(System.lineSeparator()));
    }

    public void rootValue(Object value) {
        putInternal(value);
    }

    public void put(String key, Object value) {
        putInternal(value, key);
    }

    public void putAll(ConfigMappable resource) {
        putAll(resource.asConfiguration());
    }

    public void putAll(String key, ConfigMappable resource) {
        putAll(key, resource.asConfiguration());
    }

    public void putAll(ConfigMapping config) {
        config.getAsMap().forEach((key, value) -> putInternal(value, key));
    }

    public void putAll(String key, ConfigMapping config) {
        config.getAsMap().forEach((subKey, value) -> putInternal(value, key, subKey));
    }

    public void putAll(Map<String, ?> props) {
        props.forEach((key, value) -> putInternal(value, key));
    }

    public <T extends ConfigMappable> void putList(String key, List<T> items, String name) {
        if (items.isEmpty()) {
            return;
        }

        record NamedItem(String name, ConfigMappable item) {
        }

        var named = IntStream.
                range(0, items.size())
                .mapToObj(i -> new NamedItem(name + i, items.get(i)))
                .toList();

        named.stream()
                .map(NamedItem::name)
                .reduce((x, y) -> String.join(","))
                .ifPresent(names -> put(key, names));


        named.forEach(item -> putAll(key + "." + item.name, item.item));

    }

    public <T extends ConfigMappable> void putMap(String key, Map<String, T> items) {
        items.keySet().stream()
                .reduce((x, y) -> String.join(","))
                .ifPresent(names -> put(key, names));

        items.forEach((name, item) -> putAll(key + "." + name, item));

    }

    private void putInternal(Object value, String... keys) {
        var key = prefix(keys);
        config.put(key, String.valueOf(value));
    }

    private String prefix(String... keys) {
        return Stream.concat(Stream.of(prefix), Stream.of(keys))
                .filter(Objects::nonNull)
                .filter(not(String::isBlank))
                .collect(Collectors.joining("."));
    }

    public String md5Sum() {
        byte[] digest = new byte[0];
        try {
            var md = MessageDigest.getInstance("MD5");
            digest = md.digest(getAsString().getBytes());
        }
        catch (NoSuchAlgorithmException e) {
            // This will never happen
        }
        return toHex(digest);
    }

    private String toHex(byte[] bytes) {
        var hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    @Override
    public String toString() {
        return config.toString();
    }
}
