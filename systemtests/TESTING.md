# Systemtests for Debezium Operator

This module contains system tests for the Debezium Operator.   
Testsuite leverages tooling from [Skodjob project](https://github.com/skodjob/database-performance-hub), specifically  
*Test frame* and *Database Manipulation Tool*.

## Current test process
Testsuite creates a Kubernetes namespace for each individual class and deploys MySQL, Redis, and DMT before tests  
actually start. Every resource handled via the test suite should implement the `DeployableResourceGroup` interface. Communication  
with MySQL and Redis is executed strictly through the DMT. All API calls for DMT are implemented in  
[DmtClient](src/test/java/io/debezium/operator/systemtests/resources/dmt/DmtClient.java) class.

## Running the tests
Tests can be run and debug directly from IDE or simply via Maven.

```bash  
mvn verify -Psystemtests -pl systemtests
```  

## Current state
- Testing only simple deployment of Operator Bundle install