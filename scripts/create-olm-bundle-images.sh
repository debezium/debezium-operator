#! /usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
source "$SCRIPT_DIR/functions.sh"
checkDependencies

OPTS=`getopt -o v:i: --long version:,input:,bundle-image:,push -n 'parse-options' -- "$@"`
if [ $? != 0 ] ; then echo "Failed parsing options." >&2 ; exit 1 ; fi
eval set -- "$OPTS"

# Set defaults
VERSION="all"
BUNDLE_IMAGE_PULL_URL="quay.io/debezium/operator-bundle"
INPUT_DIR_BASE="$PWD/olm/bundles"
PUSH_IMAGES=false

# Process script options
while true; do
  case "$1" in
    -v | --version )            VERSION=$2;                         shift; shift ;;
    -i | --input )              INPUT_DIR_BASE=$2;                  shift; shift ;;
    --bundle-image )            BUNDLE_IMAGE_PULL_URL=$2;           shift; shift ;;
    --push )                    PUSH_IMAGES=true;                   shift ;;
    -- ) shift; break ;;
    * ) break ;;
  esac
done

function validate_bundle() {
  path=$1
  name=$2
  operator-sdk bundle validate "$path"
  if [ $? != 0 ]; then
    echo "OLM bundle '$name' is not valid." >&2
    exit 1
  fi
}

function build_image() {
    path=$1
    image=$2
    docker build -t "$image" -f "$path/bundle.Dockerfile" "$path"
    if [[ "$PUSH_IMAGES" = true ]]; then
      docker push "$image"
    fi
}

if [[ ! -d "$INPUT_DIR_BASE" ]]; then
  echo "Input directory $INPUT_DIR_BASE not exists!"
  exit 1
fi

echo ""
echo "Creating OLM bundle image(s)"
echo "Input dir: $INPUT_DIR_BASE"
echo "Bundle version(s): $VERSION"
echo ""

if [[ $VERSION = "all" ]]; then
  BUNDLES=( $INPUT_DIR_BASE/*/ )
else
  BUNDLES=( "$INPUT_DIR_BASE/$VERSION" )
fi


for bundle in "${BUNDLES[@]}"; do
  name="$(csvName $bundle)"
  version="$(csvVersion $bundle)"
  path="$(echo "${bundle%/}")"
  image="$BUNDLE_IMAGE_PULL_URL:$version"

  echo ""
  echo "Building image for bundle '$name'"
  echo "Input dir: $path"
  echo "Bundle image: $image"
  echo ""

  validate_bundle $path $name
  build_image $path $image

  echo ""
  echo "Bundle image for '$name' build successfully!"
done;

