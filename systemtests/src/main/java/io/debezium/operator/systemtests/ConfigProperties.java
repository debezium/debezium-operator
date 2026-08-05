/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.systemtests;

public final class ConfigProperties {
    public static final String BUNDLE_PATH = System.getProperty("test.bundle.path", System.getProperty("user.dir") + "/../k8/");
    public static final Integer HTTP_POLL_TIMEOUT = Integer.valueOf(System.getProperty("test.http.poll.timeout", "20"));
    public static final Integer HTTP_POLL_INTERVAL = Integer.valueOf(System.getProperty("test.http.poll.interval", "200"));
}
