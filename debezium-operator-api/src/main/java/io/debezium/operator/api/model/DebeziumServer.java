/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.api.model;

import io.debezium.operator.api.config.ConfigMappable;
import io.debezium.operator.api.config.ConfigMapping;
import io.debezium.operator.api.model.status.DebeziumServerStatus;
import io.debezium.operator.commons.OperatorConstants;
import io.debezium.operator.docs.annotations.Documented;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;
import io.quarkiverse.operatorsdk.annotations.CSVMetadata;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.BuildableReference;

@CSVMetadata(name = OperatorConstants.CSV_INTERNAL_BUNDLE_NAME, displayName = "DebeziumServer", description = "Represents a Debezium Server")
@Version("v1alpha1")
@Group("debezium.io")
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder", lazyCollectionInitEnabled = false, refs = {
        @BuildableReference(io.fabric8.kubernetes.api.model.ObjectMeta.class),
        @BuildableReference(io.fabric8.kubernetes.client.CustomResource.class),
        @BuildableReference(DebeziumServerSpec.class)
})
@Documented(fields = {
        @Documented.Field(name = "spec", type = "DebeziumServerSpec", description = "The specification of Debezium Server"),
        @Documented.Field(name = "status", type = "DebeziumServerStatus", description = "The status of Debezium")
})
public class DebeziumServer
        extends CustomResource<DebeziumServerSpec, DebeziumServerStatus>
        implements Namespaced, ConfigMappable {

    @Override
    protected DebeziumServerSpec initSpec() {
        return new DebeziumServerSpec();
    }

    public ConfigMapping asConfiguration() {
        return spec.asConfiguration();
    }
}
