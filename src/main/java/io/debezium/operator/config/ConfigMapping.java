/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.config;

import static java.util.function.Predicate.not;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Convenience wrapper used to build properties-like configuration from arbitrary map
 */
public final class ConfigMapping {

    private final Map<String, String> config;
    private final String prefix;

    public static ConfigMapping from(Map<String, ?> properties) {
        var config = ConfigMapping.empty();
        config.put(properties);
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
        put(null, value);
    }

    public void put(String key, ConfigMappable resource) {
        var config = resource.asConfiguration();
        put(key, config.getAsMap());
    }

    public void put(String key, Object value) {
        putInternal(value, key);
    }

    public void put(ConfigMappable resource) {
        var resourceConfig = resource.asConfiguration();
        put(resourceConfig.getAsMap());
    }

    public void put(Map<String, ?> props) {
        props.forEach(this::put);
    }

    public void put(String key, Map<String, ?> props) {
        props.forEach((subKey, value) -> putInternal(value, key, subKey));
    }

    public void put(String key, String valueKey, Map<String, ?> props) {
        put(key, valueKey);
        put(valueKey, props);
    }

    public <T extends ConfigMappable> void put(String key, Collection<T> items, Function<T, String> nameExtractor) {
        items.stream()
                .map(nameExtractor)
                .reduce((x, y) -> String.join(","))
                .ifPresent(names -> put(key, names));

        items.forEach(item -> put(key, item));

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
