# Debezium Operator

Debezium operator provides an easy way to run the Debezium Server on Kubernetes or Openshift.

## Installation steps
The debezium operator currently support only per namespace installation unless installing via OLM. To install the operator to your kubernetes cluster,
simply create the descriptors available in the `k8` directory.

```bash
kubectl create -f k8/ -n $NAMESPACE 
```

_Note: In the future the operator will support OLM  and Helm chart installations._ 

### Quickstart Example

The `exmaples/postgres` directory contains an example deployment of debezium server with PostgreSQL source and kafka
sink.

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
    claimName: String # only valid and required for "persistent" type
  runtime:
    env: EnvFromSource array # https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.23/#envfromsource-v1-core
    volumes: Volume array # https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.23/#volume-v1-core
  quarkus:
    config:
      # quarkus properties 
  format:
    value:
      type: String
      config:
        # other format properties
    key:
      type: String
      config:
        # other format properties
    header:
      type: String
      config:
        # other format properties
  transforms:
    - type: String
      predicate: String
      negate: Boolean
      config:
        # other transformation properties
  predicates:
    name:
      type: String
      config:
        # other preticate properties
  sink:
    type: String
    config:
      # other sink properties
  source:
    class: String
    config:
      # other source connector properties
```

