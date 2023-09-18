function csvPath() {
  find "$1" -type f -name '*.clusterserviceversion.yaml'
}

function csvName() {
  yq ".metadata.name" $(csvPath $1)
}

function csvVersion() {
  yq ".spec.version" $(csvPath $1)
}

function csvReplaces() {
  yq ".spec.replaces" $(csvPath $1)
}

function requireGnuGetopt() {
    getopt_version="$(getopt --version)"
    if ! [[ "$getopt_version" =~ .*"getopt from util-linux".* ]]; then
      echo "GNU getopt is required"
      echo "On MacOS it can be installed by running 'brew install gnu-getopt'"
      exit 256
    fi
}

function requireYq() {
  if ! command -v yq &> /dev/null
  then
      echo "Missing yq!"
      echo "https://github.com/mikefarah/yq#install"
      exit 256
  fi
}

function checkDependencies() {
    requireGnuGetopt
    requireYq
}