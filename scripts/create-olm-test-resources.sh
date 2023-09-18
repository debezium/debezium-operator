#! /usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
source "$SCRIPT_DIR/functions.sh"
checkDependencies

OPTS=`getopt -o b:o:n:c:v: --long bundle:,output:,namespace:,channel:,version:,catalog-image: -n 'parse-options' -- "$@"`
if [ $? != 0 ] ; then echo "Failed parsing options." >&2 ; exit 1 ; fi
eval set -- "$OPTS"

# Set defaults
CATALOG_VERSION="latest"
CATALOG_IMAGE="quay.io/debezium/operator-catalog:latest"
OUTPUT_DIR_BASE="$PWD/olm/test-resources"
CATALOG_NAME="debezium-operator-catalog"
CATALOG_NAMESPACE="olm"
SUBSCRIPTION_CHANNEL="alpha"
SUBSCRIPTION_NS="operators"

# Process script options
while true; do
  case "$1" in
    -n | --namespace )          SUBSCRIPTION_NS=$2;                 shift; shift ;;
    -b | --bundle )             BUNDLE=$2;                  shift; shift ;;
    -o | --output )             OUTPUT_DIR_BASE=$2;                 shift; shift ;;
    -c | --channel )            SUBSCRIPTION_CHANNEL=$2;            shift; shift ;;
    -v | --version )            CATALOG_VERSION=$2;                 shift; shift ;;
    --watch-namespace )         WATCH_NS=$2;                        shift; shift ;;
    --catalog-image )           CATALOG_IMAGE=$2;                   shift; shift ;;
    --catalog-ns )              CATALOG_NAMESPACE=$2;               shift; shift ;;
    -- ) shift; break ;;
    * ) break ;;
  esac
done

rm -rf "$OUTPUT_DIR_BASE"
mkdir -p "$OUTPUT_DIR_BASE"


echo ""
echo "Creating OLM test resources"
echo "Output dir: $OUTPUT_DIR_BASE"
echo ""

cat << EOF >> "$OUTPUT_DIR_BASE/catalog.yaml"
apiVersion: operators.coreos.com/v1alpha1
kind: CatalogSource
metadata:
  name: $CATALOG_NAME
  namespace: $CATALOG_NAMESPACE
spec:
  grpcPodConfig:
    securityContextConfig: restricted
  sourceType: grpc
  image: $CATALOG_IMAGE
  displayName: Debezium Test Catalog
  publisher: Me
  updateStrategy:
    registryPoll:
      interval: 10m
EOF

cat << EOF >> "$OUTPUT_DIR_BASE/subscription.yaml"
apiVersion: operators.coreos.com/v1alpha1
kind: Subscription
metadata:
  name: debezium-operator-subscription
  namespace: $SUBSCRIPTION_NS
spec:
  installPlanApproval: Automatic
  name: debezium-operator
  source: $CATALOG_NAME
  sourceNamespace: $CATALOG_NAMESPACE
  startingCSV: $BUNDLE
EOF



