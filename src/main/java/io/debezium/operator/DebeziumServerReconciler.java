/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;

import io.debezium.operator.dependent.ConfigMapDependent;
import io.debezium.operator.dependent.DeploymentDependent;
import io.debezium.operator.dependent.RoleBindingDependent;
import io.debezium.operator.dependent.ServiceAccountDependent;
import io.debezium.operator.dependent.conditions.DeploymentReady;
import io.debezium.operator.model.status.Condition;
import io.debezium.operator.model.status.DebeziumServerStatus;
import io.javaoperatorsdk.operator.api.reconciler.Constants;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import io.quarkus.logging.Log;

@ControllerConfiguration(namespaces = Constants.WATCH_CURRENT_NAMESPACE, name = "debeziumserver", dependents = {
        @Dependent(name = "service-account", type = ServiceAccountDependent.class),
        @Dependent(name = "role-binding", type = RoleBindingDependent.class, dependsOn = { "service-account" }),
        @Dependent(name = "config", type = ConfigMapDependent.class),
        @Dependent(name = "deployment", type = DeploymentDependent.class, dependsOn = {
                "config",
                "role-binding"
        }, readyPostcondition = DeploymentReady.class),
})
public class DebeziumServerReconciler implements Reconciler<DebeziumServer> {

    @Override
    public UpdateControl<DebeziumServer> reconcile(DebeziumServer debeziumServer, Context<DebeziumServer> context) {
        var name = debeziumServer.getMetadata().getName();
        return context.managedDependentResourceContext().getWorkflowReconcileResult()
                .map(result -> {
                    if (result.allDependentResourcesReady()) {
                        Log.infof("Server %s is ready", name);
                        initializeReadyStatus(debeziumServer);
                        return UpdateControl.patchStatus(debeziumServer);
                    }
                    else {
                        var delay = Duration.ofSeconds(10);
                        Log.infof("Server %s not ready yet, rescheduling after %ds", name, delay.toSeconds());
                        initializeNotReadyStatus(debeziumServer);
                        return UpdateControl.patchStatus(debeziumServer).rescheduleAfter(delay);
                    }
                }).orElseThrow();
    }

    private void initializeReadyStatus(DebeziumServer debeziumServer) {
        var condition = new Condition();
        condition.setType("Ready");
        condition.setStatus("True");
        condition.setMessage("Server %s is ready".formatted(debeziumServer.getMetadata().getName()));

        initializeConditions(debeziumServer, condition);
    }

    private void initializeNotReadyStatus(DebeziumServer debeziumServer) {
        var condition = new Condition();
        condition.setType("Ready");
        condition.setStatus("False");
        condition.setMessage("Server %s deployment in progress".formatted(debeziumServer.getMetadata().getName()));

        initializeConditions(debeziumServer, condition);
    }

    private void initializeConditions(DebeziumServer debeziumServer, Condition... conditions) {
        var list = new ArrayList<>(Arrays.asList(conditions));

        var status = new DebeziumServerStatus();
        status.setConditions(list);

        debeziumServer.setStatus(status);
    }
}
