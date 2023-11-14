/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model;

import java.util.HashMap;
import java.util.Map;

public final class CommonLabels {

    public static final String KEY_APP_K8_INSTANCE = "app.kubernetes.io/instance";
    public static final String KEY_APP_K8_MANAGED_BY = "app.kubernetes.io/managed-by";
    public static final String KEY_APP_K8_COMPONENT = "app.kubernetes.io/component";
    public static final String KEY_DBZ_INSTANCE = "debezium.io/instance";
    public static final String KEY_DBZ_COMPONENT = "debezium.io/component";
    public static final String KEY_DBZ_CLASSIFIER = "debezium.io/classifier";

    private Map<String, String> map = new HashMap<>();

    public static CommonLabels serverComponent(String instance) {
        return new CommonLabels()
                .withManagedBy("debezium-operator")
                .withComponent("DebeziumServer")
                .withInstance(instance);
    }

    public CommonLabels withComponent(String component) {
        map.put(KEY_APP_K8_COMPONENT, component);
        map.put(KEY_DBZ_COMPONENT, component);
        return this;
    }

    public CommonLabels withInstance(String instance) {
        map.put(KEY_APP_K8_INSTANCE, instance);
        map.put(KEY_DBZ_INSTANCE, instance);
        return this;
    }

    public CommonLabels withManagedBy(String managedBy) {
        map.put(KEY_APP_K8_MANAGED_BY, managedBy);
        return this;
    }

    public CommonLabels withDbzClassifier(String classifier) {
        map.put(KEY_DBZ_CLASSIFIER, classifier);
        return this;
    }

    public Map<String, String> getMap() {
        return new HashMap<>(map);
    }

}
