/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator;

import io.debezium.operator.dependent.ConfigMapDependent;
import io.debezium.operator.dependent.DeploymentDependent;
import io.debezium.operator.dependent.ServiceDependent;
import io.javaoperatorsdk.operator.api.reconciler.Constants;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import io.quarkus.logging.Log;

@ControllerConfiguration(namespaces = Constants.WATCH_CURRENT_NAMESPACE, name = "debeziumserver", dependents = {
        @Dependent(type = ConfigMapDependent.class),
        @Dependent(type = DeploymentDependent.class),
        @Dependent(type = ServiceDependent.class)
})
public class DebeziumServerReconciler implements Reconciler<DebeziumServer> {

    @Override
    public UpdateControl<DebeziumServer> reconcile(DebeziumServer debeziumServer, Context<DebeziumServer> context) {
        Log.infof("Registered Debezium Server: %s", debeziumServer.getMetadata().getName());

        return UpdateControl.noUpdate();
    }

}
