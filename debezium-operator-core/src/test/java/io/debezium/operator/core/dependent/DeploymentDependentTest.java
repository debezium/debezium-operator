/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.core.dependent;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class DeploymentDependentTest {

    @ParameterizedTest
    @CsvSource({
            "io.debezium.connector.postgresql.PostgresConnector, postgresql",
            "io.debezium.connector.mysql.MySqlConnector, mysql",
            "io.debezium.connector.mongodb.MongoDbConnector, mongodb",
            "io.debezium.connector.sqlserver.SqlServerConnector, sqlserver",
            "io.debezium.connector.oracle.OracleConnector, oracle",
    })
    void shouldExtractConnectorType(String sourceClass, String expectedType) {
        assertThat(DeploymentDependent.extractConnectorType(sourceClass)).isEqualTo(expectedType);
    }

    @Test
    void shouldReturnUnknownForNullSourceClass() {
        assertThat(DeploymentDependent.extractConnectorType(null)).isEqualTo("unknown");
    }

    @Test
    void shouldReturnUnknownForBlankSourceClass() {
        assertThat(DeploymentDependent.extractConnectorType("")).isEqualTo("unknown");
        assertThat(DeploymentDependent.extractConnectorType("  ")).isEqualTo("unknown");
    }

    @Test
    void shouldReturnUnknownForSingleSegmentClass() {
        assertThat(DeploymentDependent.extractConnectorType("PostgresConnector")).isEqualTo("unknown");
    }
}
