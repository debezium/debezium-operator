/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.commons.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Useful String utilities
 */
public final class StringUtils {

    public static final String EMPTY = "";

    /**
     * Joins map into string
     *
     * @param map map to be joined
     * @param kvSep separator used to join key and value
     * @param sep separator used to join entries
     * @return joined map
     */
    public static String join(Map<String, ?> map, String kvSep, String sep) {
        return map.entrySet()
                .stream()
                .map(e -> joinEntry(e.getKey(), e.getValue(), kvSep))
                .collect(Collectors.joining(sep));
    }

    /**
     * Joins map into string of java properties as given on command line
     * e.g. {-Dprop: value, -Xmx128M: null} becomes "-Dprop=value -Xmx128M";
     *
     * @param map map to be joined
     * @return string of java properties as given on command line
     */
    public static String joinAsJavaOpts(Map<String, ?> map) {
        return join(map, "=", " ");
    }

    /**
     * Splits string into map based on given parameters
     *
     * @param input input string
     * @param kvSep separator used to split key and value
     * @param sep separator used to split entries
     * @return map
     */
    public static Map<String, String> splitToMap(String input, String kvSep, String sep) {
        return Stream.of(input)
                .filter(Predicate.not(String::isBlank))
                .map(i -> i.split(Pattern.quote(sep)))
                .flatMap(Arrays::stream)
                .map(elem -> splitEntry(elem, kvSep))
                .collect(HashMap::new, (m, v) -> m.put(v[0], v[1]), HashMap::putAll);
    }

    /**
     * Splits string of java properties as given on command line into map
     * e.g. "-Dprop=value -Xmx128M"  becomes {-Dprop: value, -Xmx128M: ""}
     *
     * @param input input string
     * @return map
     */
    public static Map<String, String> splitJavaOpts(String input) {
        return splitToMap(input, "=", " ");
    }

    private static String[] splitEntry(String entry, String kvSep) {
        var result = new String[]{ EMPTY, EMPTY };
        var keyValue = entry.split(Pattern.quote(kvSep), 2);
        System.arraycopy(keyValue, 0, result, 0, keyValue.length);

        return result;
    }

    private static String joinEntry(String key, Object value, String kvSep) {
        var valueAsString = String.valueOf(value);
        if (value == null || valueAsString.isEmpty()) {
            return key;
        }
        return key + kvSep + valueAsString;
    }

    /**
     * Intentionally private
     */
    private StringUtils() {
    }
}
