/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.config;

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

import io.fabric8.kubernetes.api.model.HasMetadata;

/**
 * Convenience wrapper used to build properties-like configuration from arbitrary map
 */
public final class ConfigMapping<P extends HasMetadata> {

    private final P primary;

    /**
     * Represents a key in the configuration
     */
    public enum KeyType {
        /**
         * The key is relative to the current prefix
         */
        RELATIVE,

        /**
         * The key is absolute and will never be prefixed (unless specifically documented),
         * including situations when the mapping is put into another mapping
         */
        ABSOLUTE;
    }

    public record Key(String name, KeyType type) {
        public static Key rel(String key) {
            return new Key(key, KeyType.RELATIVE);
        }

        public static Key abs(String key) {
            return new Key(key, KeyType.ABSOLUTE);
        }

        public static Key root() {
            return new Key(null, null);
        }

        public Key asAbs() {
            return abs(name);
        }

        public Key asRel() {
            return rel(name);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final Map<Key, String> config;
    private final String prefix;

    public static <P extends HasMetadata> ConfigMapping<P> from(P primary, Map<String, ?> properties) {
        var config = ConfigMapping.empty(primary);
        config.putAll(properties);
        return config;
    }

    public static <P extends HasMetadata> ConfigMapping<P> empty(P primary) {
        return new ConfigMapping<>(primary, null);
    }

    public static <P extends HasMetadata> ConfigMapping<P> prefixed(P primary, String prefix) {
        return new ConfigMapping<>(primary, prefix);
    }

    public ConfigMapping(P primary, String prefix) {
        this.config = new HashMap<>();
        this.prefix = prefix;
        this.primary = primary;
    }

    /**
     * Creates new ConfigMapping, with the same prefix as this one, but sets all keys as absolute
     *
     * @return new ConfigMapping with all keys as absolute
     */
    public ConfigMapping<P> asAbsolute() {
        return ConfigMapping.prefixed(primary, prefix).putAllAbs(this);
    }

    /**
     * Creates new ConfigMapping, with the same prefix as this one, but sets all keys as relative
     *
     * @return new ConfigMapping with all keys as relative
     */
    public ConfigMapping<P> asRelative() {
        return ConfigMapping.prefixed(primary, prefix).putAllRel(this);
    }

    public Map<String, String> getAsMapSimple() {
        return config.entrySet()
                .stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));
    }

    public Map<Key, String> getAsMap() {
        return config;
    }

    public String getAsString() {
        return config.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .sorted()
                .collect(Collectors.joining(System.lineSeparator()));
    }

    public ConfigMapping<P> rootValue(Object value) {
        putInternal(value, Key.root());
        return this;
    }

    public ConfigMapping<P> put(String key, Object value) {
        putInternal(value, Key.rel(key));
        return this;
    }

    /**
     * Puts the value with the given key as absolute key (that is the key will never be prefixed)
     *
     * @param key the absolute key to put the value under
     * @param value the value to put
     * @return this mapping
     */
    public ConfigMapping<P> putAbs(String key, Object value) {
        putInternal(value, Key.abs(key));
        return this;
    }

    public ConfigMapping<P> putAll(ConfigMappable<P> resource) {
        return putAll(resource.asConfiguration(primary));
    }

    public ConfigMapping<P> putAll(String key, ConfigMappable<P> resource) {
        return putAll(key, resource.asConfiguration(primary));
    }

    public ConfigMapping<P> putAll(ConfigMapping<?> config) {
        config.getAsMap().forEach((key, value) -> putInternal(value, key));
        return this;
    }

    public ConfigMapping<P> putAll(String key, ConfigMapping<?> config) {
        config.getAsMap().forEach((subKey, value) -> putInternal(value, key, subKey));
        return this;
    }

    public ConfigMapping<P> putAll(Map<String, ?> props) {
        props.forEach((key, value) -> putInternal(value, Key.rel(key)));
        return this;
    }

    /**
     * Puts all values from the given configuration, but sets all keys as absolute
     * @param config the configuration to put
     * @return this mapping
     */
    public ConfigMapping<P> putAllAbs(ConfigMapping<?> config) {
        config.getAsMap().forEach((key, value) -> putInternal(value, key.asAbs()));
        return this;
    }

    /**
     * Puts all values from the given configuration, but sets all keys as relative
     * @param config the configuration to put
     * @return this mapping
     */
    public ConfigMapping<P> putAllRel(ConfigMapping<?> config) {
        config.getAsMap().forEach((key, value) -> putInternal(value, key.asRel()));
        return this;
    }

    @SuppressWarnings("unchecked")
    public ConfigMapping<P> putList(String key, List<? extends ConfigMappable<P>> items, String name) {
        if (items.isEmpty()) {
            return this;
        }

        record NamedItem(String name, ConfigMappable<?> item) {
        }

        var named = IntStream.range(0, items.size())
                .mapToObj(i -> new NamedItem(name + i, items.get(i)))
                .toList();

        named.stream()
                .map(NamedItem::name)
                .reduce((x, y) -> x + "," + y)
                .ifPresent(names -> put(key, names));

        named.forEach(item -> putAll(key + "." + item.name, (ConfigMappable<P>) item.item));
        return this;
    }

    public ConfigMapping<P> putMap(String key, Map<String, ? extends ConfigMappable<P>> items) {
        items.keySet().stream()
                .reduce((x, y) -> x + "," + y)
                .ifPresent(names -> put(key, names));

        items.forEach((name, item) -> putAll(key + "." + name, item));
        return this;
    }

    private void putInternal(Object value, Key key) {
        putInternal(value, null, key);
    }

    private void putInternal(Object value, String key, Key subKey) {
        if (value == null) {
            return;
        }
        var combined = prefix(key, subKey);
        config.put(combined, String.valueOf(value));
    }

    private Key prefix(String key, Key subKey) {
        if (subKey.type == KeyType.ABSOLUTE) {
            return subKey;
        }

        var combined = Stream.concat(Stream.of(prefix), Stream.of(key, subKey.name))
                .filter(Objects::nonNull)
                .filter(not(String::isBlank))
                .collect(Collectors.joining("."));

        return Key.rel(combined);
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
