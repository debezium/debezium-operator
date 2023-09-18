#! /usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
source "$SCRIPT_DIR/functions.sh"
checkDependencies

OPTS=`getopt -o b:i:o:n:c:v:f --long bundle:,input:,output:,name:,channel:,version:,catalog-image:,bundle-image:,force,push -n 'parse-options' -- "$@"`
if [ $? != 0 ] ; then echo "Failed parsing options." >&2 ; exit 1 ; fi
eval set -- "$OPTS"

# Set defaults
BUNDLE_VERSION="all"
BUNDLE_IMAGE_PULL_URL="quay.io/debezium/operator-bundle"
CATALOG_VERSION="latest"
CATALOG_IMAGE_PULL_URL="quay.io/debezium/operator-catalog"
INPUT_DIR_BASE="$PWD/olm/bundles"
OUTPUT_DIR_BASE="$PWD/olm/catalog"
CATALOG_NAME="debezium-operator"
CATALOG_CHANNEL="alpha"
PUSH_IMAGE=false
FORCE=false

# Process script options
while true; do
  case "$1" in
    -n | --name )               CATALOG_NAME=$2;                    shift; shift ;;
    -b | --bundle )             BUNDLE_VERSION=$2;                  shift; shift ;;
    -i | --input )              INPUT_DIR_BASE=$2;                  shift; shift ;;
    -o | --output )             OUTPUT_DIR_BASE=$2;                 shift; shift ;;
    -c | --channel )            CATALOG_CHANNEL=$2;                 shift; shift ;;
    -v | --version )            CATALOG_VERSION=$2;                 shift; shift ;;
    -f | --force )              FORCE=true;                         shift ;;
    --push )                    PUSH_IMAGE=true;                    shift ;;
    --catalog-image )           CATALOG_IMAGE=$2;                   shift; shift ;;
    --bundle-image )            BUNDLE_IMAGE_PULL_URL=$2;           shift; shift ;;
    -- ) shift; break ;;
    * ) break ;;
  esac
done

if [[ ! -d "$INPUT_DIR_BASE" ]]; then
  echo "Input directory $INPUT_DIR_BASE not exists!"
  exit 1
fi

# Set variables
CATALOG_DIR="$OUTPUT_DIR_BASE/$CATALOG_NAME"
CATALOG_MANIFEST_FILE="$CATALOG_DIR/operator.yaml"
CATALOG_DOCKER_FILE="$OUTPUT_DIR_BASE/$CATALOG_NAME.Dockerfile"
CATALOG_IMAGE="$CATALOG_IMAGE_PULL_URL:$CATALOG_VERSION"

echo ""
echo "Creating OLM catalog"
echo "Bundle input dir: $INPUT_DIR_BASE"
echo "Bundle bundle(s): $BUNDLE_VERSION"
echo ""

if [[ $BUNDLE_VERSION = "all" ]]; then
  BUNDLES=( $INPUT_DIR_BASE/*/ )
else
  BUNDLES=( "$INPUT_DIR_BASE/$BUNDLE_VERSION" )
fi

if [[ -d "$CATALOG_DIR" && "$FORCE" = true ]]; then
  echo "Removing exiting catalog directory '$CATALOG_DIR'"
  rm -rf "$CATALOG_DIR"
fi

if [[ -f "$CATALOG_DOCKER_FILE" && "$FORCE" = true ]]; then
  echo "Removing exiting catalog dockerfile '$CATALOG_DOCKER_FILE'"
  rm -rf "$CATALOG_DOCKER_FILE"
fi

if [[ -d "$CATALOG_DIR" ]]; then
  echo "Directory $CATALOG_DIR already exists!"
  echo "Use -f / --force to overwrite"
  exit 2
fi

if [[ -d "$CATALOG_DOCKER_FILE" ]]; then
  echo "Dockerfile $CATALOG_DOCKER_FILE already exists!"
  echo "Use -f / --force to overwrite"
  exit 3
fi

# Generate dockerfile and initialize catalog manifest
mkdir -p "$CATALOG_DIR"
opm generate dockerfile "$CATALOG_DIR"
opm init "$CATALOG_NAME" --default-channel="$CATALOG_CHANNEL" --output yaml > "$CATALOG_MANIFEST_FILE"

# Render each bundle
for bundle in "${BUNDLES[@]}"; do
  name="$(csvName $bundle)"
  version="$(csvVersion $bundle)"
  image="$BUNDLE_IMAGE_PULL_URL:$version"

  echo ""
  echo "Rendering bundle '$name'"
  echo "Bundle directory: " $bundle
  echo "Bundle image: $image "
  echo ""

  opm render "$image" --output=yaml >> "$CATALOG_MANIFEST_FILE"
done;


# Write out channel declaration
echo ""
echo "Creating channel '$name'"
echo ""
cat << EOF >> "$CATALOG_MANIFEST_FILE"
---
schema: olm.channel
package: $CATALOG_NAME
name: $CATALOG_CHANNEL
entries:
EOF

# Write out channel entries
for bundle in "${BUNDLES[@]}"; do
  name="$(csvName $bundle)"
  replaces="$(csvReplaces $bundle)"

  echo " - name: $name" >> "$CATALOG_MANIFEST_FILE"
  [ $replaces != "null" ] && echo "   replaces: $replaces" >> "$CATALOG_MANIFEST_FILE"
done;


# Validate generated catalog
opm validate "$CATALOG_DIR"

# Build image
docker build -t "$CATALOG_IMAGE" -f "$CATALOG_DOCKER_FILE" "$OUTPUT_DIR_BASE"

echo ""
echo "Catalog created!"
echo "Output dir: $OUTPUT_DIR_BASE"
echo "Name: $CATALOG_NAME"
echo "Channel: $CATALOG_CHANNEL"
echo "Image: $CATALOG_IMAGE"
echo ""


if [[ "$PUSH_IMAGE" = true ]]; then
    echo ""
    echo "Pushing image: $CATALOG_IMAGE"
    docker push "$CATALOG_IMAGE"
fi
