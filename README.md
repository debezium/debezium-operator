# Debezium Operator

Debezium operator provides an easy way to run the Debezium Server on Kubernetes or Openshift.

## Installation steps
The debezium operator currently support per namespace installation. To install the operator to your kubernetes cluster, simply create the descriptors available in the `k8` directory.

```bash
kubectl create -f k8/ -n $NAMESPACE
```

### Quickstart Example
The `exmaples/postgres` directory contains an example deployment of debezium server with PostgreSQL source and kafka sink.

```bash
# Install Strimzi Kafka operator
kubectl create -f "https://strimzi.io/install/latest?namespace=$NAMESPACE" -n $NAMESPACE

# Deploy PostgreSQL, Kafka and Debezium Server
kubectl create -f examples/postgres/ -n $NAMESPACE    
```

## DebeziumServerSpec Reference
```yaml
spec:
    version: String 
    image: String # exclusive with version
    storage:
      type: persistent | ephemeral  # enum
      claimName: String # only valid and required for persistent
    externalConfiguration:
      env: EnvFromSource array # https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.23/#envfromsource-v1-core
      volumes: Volume array # https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.23/#volume-v1-core
    quarkus:
      # quarkus properties 
    format:
      value:
        type: String
        # other format properties
      key:
        type: String
      header:
        type: String 
    transforms:
      - name: String
        type: String
        predicate: String
        negate: Boolean
        # other transformation properties
    predicates: 
      - name: String
        type: String
        # other preticate properties
    sink:
      type: String
      # other sink properties
    source:
      class: String
      # other source connector properties
```