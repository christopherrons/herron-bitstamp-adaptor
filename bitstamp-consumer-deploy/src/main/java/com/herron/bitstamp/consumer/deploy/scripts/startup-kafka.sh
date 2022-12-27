#!/bin/bash
source setenv-kafka.sh

function verifyDirectory() {
  deployDirectory="/home/$LXC_USER/deploy"
  if [ "$PWD" != "$deployDirectory" ]; then
    echo "Aborted: File has to be run from $deployDirectory but was run from $PWD"
    exit
  fi
}

function checkIfRunning() {
  versionFile="/home/$LXC_USER/deploy/$COMMON_VERSION_FILE"
  if test -f "$versionFile"; then
      echo "Kafka running aborting..."
      exit
  fi
}

# Set the version
version=current
if [[ ${1+x} ]]
then
    version=$1
fi

# Persist version running
verifyDirectory
checkIfRunning
versionFile="$COMMON_VERSION_FILE"
touch "$versionFile"
echo "$version" > "$versionFile"

cd ../kafka_2.13-3.3.1 || exit
KAFKA_CLUSTER_ID="$(bin/kafka-storage.sh random-uuid)"
bin/kafka-storage.sh format -t "$KAFKA_CLUSTER_ID" -c config/kraft/server.properties
bin/kafka-server-start.sh config/kraft/server.properties
cd - || exit
