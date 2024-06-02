/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;

import io.debezium.operator.api.model.DebeziumServer;
import io.debezium.operator.api.model.status.Condition;
import io.debezium.operator.api.model.status.DebeziumServerStatus;
import io.debezium.operator.commons.OperatorConstants;
import io.debezium.operator.core.dependent.ConfigMapDependent;
import io.debezium.operator.core.dependent.DeploymentDependent;
import io.debezium.operator.core.dependent.JmxExporterServiceDependent;
import io.debezium.operator.core.dependent.JmxServiceDependent;
import io.debezium.operator.core.dependent.PvcDependent;
import io.debezium.operator.core.dependent.RoleBindingDependent;
import io.debezium.operator.core.dependent.RoleDependent;
import io.debezium.operator.core.dependent.ServiceAccountDependent;
import io.debezium.operator.core.dependent.conditions.CreatePvc;
import io.debezium.operator.core.dependent.conditions.CreateServiceAccount;
import io.debezium.operator.core.dependent.conditions.DeploymentReady;
import io.debezium.operator.core.dependent.conditions.JmxEnabled;
import io.debezium.operator.core.dependent.conditions.JmxExporterEnabled;
import io.debezium.operator.core.dependent.conditions.PvcReady;
import io.javaoperatorsdk.operator.api.reconciler.Constants;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import io.quarkiverse.operatorsdk.annotations.CSVMetadata;
import io.quarkiverse.operatorsdk.annotations.RBACRule;
import io.quarkiverse.operatorsdk.annotations.RBACVerbs;
import io.quarkus.logging.Log;

@ControllerConfiguration(namespaces = Constants.WATCH_CURRENT_NAMESPACE, name = "debeziumserver", dependents = {
        @Dependent(name = "service-account", type = ServiceAccountDependent.class, reconcilePrecondition = CreateServiceAccount.class),
        @Dependent(name = "pvc", type = PvcDependent.class, reconcilePrecondition = CreatePvc.class),
        @Dependent(name = "role", type = RoleDependent.class),
        @Dependent(name = "role-binding", type = RoleBindingDependent.class, dependsOn = {
                "service-account",
                "role"
        }),
        @Dependent(name = "config", type = ConfigMapDependent.class),
        @Dependent(name = "deployment", type = DeploymentDependent.class, dependsOn = {
                "config",
                "role-binding",
        }, reconcilePrecondition = PvcReady.class, readyPostcondition = DeploymentReady.class),
        @Dependent(name = "jmx-service", type = JmxServiceDependent.class, dependsOn = "deployment", reconcilePrecondition = JmxEnabled.class),
        @Dependent(name = "jmx-exporter-service", type = JmxExporterServiceDependent.class, dependsOn = "deployment", reconcilePrecondition = JmxExporterEnabled.class)
})
@CSVMetadata(name = OperatorConstants.CSV_INTERNAL_BUNDLE_NAME)
@RBACRule(verbs = { RBACVerbs.GET, RBACVerbs.LIST, RBACVerbs.WATCH }, apiGroups = "", resources = "secrets")
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
