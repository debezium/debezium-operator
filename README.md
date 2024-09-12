# Debezium Operator

Debezium operator provides an easy way to run the Debezium Server on Kubernetes or Openshift.

## Installation steps
The operator can be installed via OLM, Helm chart or by creating the resources directly. 

### Helm Chart Installation
Helm is the preferred way to install the operator releases. The operator is available in the [Debezium Helm Chart Repository](https://charts.debezium.io).

```bash
helm repo add debezium https://charts.debezium.io
helm install my-debezium-operator debezium/debezium-operator --version $DO_VERSION -n $NAMESPACE
```
Available versions can be found in the [Debezium Helm Chart Repository](https://charts.debezium.io/index.yaml).

### OLM Installation
Released versions of the operator can be installed via the Operator Lifecycle Manager (OLM). The operator is available in the [OperatorHub](https://operatorhub.io/operator/debezium-operator).

### Direct Installation
This method currently support only per namespace installation. To install the operator to your Kubernetes cluster, simply create the descriptors available in the `k8` directory.

```bash
kubectl create -f k8/ -n $NAMESPACE 
```

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
The snippet bellow provides a rough outline of the `DebeziumServer` spec. See the [full API reference](docs/reference.adoc) for more details.


```yaml
spec:
  version: String
  image: String # exclusive with version
  runtime:
    environment:
      vars:
        - name: String
          value: String
      from: EnvFromSource array # https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.23/#envfromsource-v1-core
    storage:
      data:
        type: persistent | ephemeral  # enum
        claimName: String # only valid and required for "persistent" type
      external: Volume array # https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.23/#volume-v1-core
    jmx:
      enabled: boolean
      port: int # defaults to 1099
    templates:
      container:
        resources: # https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.23/#resourcerequirements-v1-core
        securityContext: # https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.23/#securitycontext-v1-core
      pod:
        metadata:
          annotations: Map<String, String>
          labels: Map<String, String>
        imagePullSecrets: List # https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.23/#localobjectreference-v1-core
        affinity: # https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.23/#affinity-v1-core
        securityContext: # https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.23/#podsecuritycontext-v1-core
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

