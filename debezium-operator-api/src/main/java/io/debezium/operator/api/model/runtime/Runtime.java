/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model.runtime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.debezium.operator.api.model.runtime.jmx.JmxConfig;
import io.debezium.operator.api.model.runtime.metrics.Metrics;
import io.debezium.operator.api.model.runtime.storage.RuntimeStorage;
import io.debezium.operator.api.model.runtime.templates.Templates;
import io.debezium.operator.docs.annotations.Documented;
import io.sundr.builder.annotations.Buildable;

@JsonPropertyOrder({ "storage", "environment", "jmx", "templates" })
@Documented
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder", lazyCollectionInitEnabled = false)
public class Runtime {
    @JsonPropertyDescription("Storage configuration")
    private RuntimeStorage storage;

    @JsonPropertyDescription("Additional environment variables used by this Debezium Server.")
    private RuntimeEnvironment environment;

    @JsonPropertyDescription("JMX configuration.")
    private JmxConfig jmx;

    @JsonPropertyDescription("Debezium Server resource templates.")
    private Templates templates;

    @JsonPropertyDescription("An existing service account used to run the Debezium Server pod")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String serviceAccount;

    @JsonPropertyDescription("Metrics configuration")
    private Metrics metrics;

    public Runtime() {
        this.storage = new RuntimeStorage();
        this.environment = new RuntimeEnvironment();
        this.jmx = new JmxConfig();
        this.templates = new Templates();
        this.metrics = new Metrics();
    }

    public Templates getTemplates() {
        return templates;
    }

    public void setTemplates(Templates templates) {
        this.templates = templates;
    }

    public JmxConfig getJmx() {
        return jmx;
    }

    public void setJmx(JmxConfig jmx) {
        this.jmx = jmx;
    }

    public String getServiceAccount() {
        return serviceAccount;
    }

    public void setServiceAccount(String serviceAccount) {
        this.serviceAccount = serviceAccount;
    }

    public RuntimeStorage getStorage() {
        return storage;
    }

    public void setStorage(RuntimeStorage storage) {
        this.storage = storage;
    }

    public RuntimeEnvironment getEnvironment() {
        return environment;
    }

    public void setEnvironment(RuntimeEnvironment environment) {
        this.environment = environment;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public void setMetrics(Metrics metrics) {
        this.metrics = metrics;
    }
}
