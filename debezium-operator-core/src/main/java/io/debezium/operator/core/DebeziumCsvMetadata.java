/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core;

import io.debezium.operator.commons.OperatorConstants;
import io.quarkiverse.operatorsdk.annotations.CSVMetadata;
import io.quarkiverse.operatorsdk.annotations.CSVMetadata.Annotations;
import io.quarkiverse.operatorsdk.annotations.CSVMetadata.Icon;
import io.quarkiverse.operatorsdk.annotations.CSVMetadata.InstallMode;
import io.quarkiverse.operatorsdk.annotations.CSVMetadata.Link;
import io.quarkiverse.operatorsdk.annotations.CSVMetadata.Maintainer;
import io.quarkiverse.operatorsdk.annotations.CSVMetadata.Provider;
import io.quarkiverse.operatorsdk.annotations.SharedCSVMetadata;

// @formatter:off
@CSVMetadata(
        name = OperatorConstants.CSV_INTERNAL_BUNDLE_NAME,
        displayName = "Debezium Operator",
        icon = @Icon(fileName = "debezium-icon.svg"),
        provider = @Provider(name = "Debezium Authors", url = "https://debezium.io/"),
        maintainers = @Maintainer(name = "Debezium Authors", email = "debezium@googlegroups.com"),
        keywords = {
            "Debezium",
            "CDC",
            "Data",
            "Streaming"},
        links = {
            @Link(url = "https://debezium.io/", name = "Debezium"),
            @Link(url = "https://debezium.io/documentation/reference/stable/", name = "Documentation"),
            @Link(url = "https://debezium.zulipchat.com", name = "Debezium Zulip Chat")},
        installModes = {
            @InstallMode(type = "OwnNamespace"),
            @InstallMode(type = "SingleNamespace"),
            @InstallMode(type = "AllNamespaces"),
            @InstallMode(type = "MultiNamespace")},
        annotations = @Annotations(
                repository = "${olm.bundle.repository}",
                capabilities = "Basic Install",
                categories = "Big Data, Database, Integration & Delivery, Streaming & Messaging",
                containerImage = "${olm.bundle.containerImage}",
                others = {
                    @Annotations.Annotation(
                            name = "createdAt",
                            value = "${olm.bundle.createdAt}"),
                    @Annotations.Annotation(
                            name = "support",
                            value = "Debezium Authors"),
                    @Annotations.Annotation(
                            name = "description",
                            value = "An Operator for installing and managing Debezium")},
            almExamples ="""
                    [
                        {
                          "apiVersion": "debezium.io/v1alpha1",
                          "kind": "DebeziumServer",
                          "metadata": {
                            "name": "debezium-test"
                          },
                          "spec": {
                            "quarkus": {
                              "config": {
                                "log.console.json": false
                              }
                            },
                            "sink": {
                              "type": "kafka",
                              "config": {
                                "producer.bootstrap.servers": "dbz-kafka-kafka-bootstrap.debezium:9092",
                                "producer.key.serializer": "org.apache.kafka.common.serialization.StringSerializer",
                                "producer.value.serializer": "org.apache.kafka.common.serialization.StringSerializer"
                              }
                            },
                            "source": {
                              "class": "io.debezium.connector.mongodb.MongoDbConnector",
                              "config": {
                                "topic.prefix": "dbserver1",
                                "offset.storage.file.filename": "/debezium/data/offsets.dat",
                                "database.history": "io.debezium.relational.history.FileDatabaseHistory",
                                "mongodb.connection.string": "mongodb://debezium:dbz@mongo.debezium:27017/?replicaSet=rs0"
                              }
                            }
                          }
                        }
                    ]
                    """),
        description = "Debezium is an open source distributed platform for change data capture. " +
                "Start it up, point it at your databases, and your apps can start responding " +
                "to all of the inserts, updates, and deletes that other apps commit to your databases")
public class DebeziumCsvMetadata implements SharedCSVMetadata {
}
