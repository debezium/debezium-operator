/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator;

import java.time.Duration;
import java.util.ArrayList;

import io.debezium.operator.dependent.ConfigMapDependent;
import io.debezium.operator.dependent.DeploymentDependent;
import io.debezium.operator.dependent.ServiceDependent;
import io.debezium.operator.model.status.Condition;
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
        var name = debeziumServer.getMetadata().getName();
        return context.managedDependentResourceContext().getWorkflowReconcileResult()
                .map(result -> {
                    if (result.allDependentResourcesReady()) {
                        Log.infof("Server %s is ready", name);
                        addReadyCondition(debeziumServer);
                        return UpdateControl.patchStatus(debeziumServer);
                    }
                    else {
                        var delay = Duration.ofSeconds(10);
                        Log.infof("Server %s not ready yet, rescheduling after %ds", name, delay.toSeconds());
                        return UpdateControl.<DebeziumServer> noUpdate().rescheduleAfter(delay);
                    }
                }).orElseThrow();
    }

    private void addReadyCondition(DebeziumServer debeziumServer) {
        var readyCondition = new Condition();
        readyCondition.setType("Ready");
        readyCondition.setStatus("True");
        readyCondition.setMessage("Server %s is ready".formatted(debeziumServer.getMetadata().getName()));

        var conditions = new ArrayList<Condition>();
        conditions.add(readyCondition);

        debeziumServer.getStatus().setConditions(conditions);
    }
}
