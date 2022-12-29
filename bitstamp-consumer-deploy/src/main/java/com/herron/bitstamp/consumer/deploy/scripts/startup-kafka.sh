#!/bin/bash
source setenv-kafka.sh

cd kafka_2.13-3.3.1 || exit
KAFKA_CLUSTER_ID="$(bin/kafka-storage.sh random-uuid)"
bin/kafka-storage.sh format -t "$KAFKA_CLUSTER_ID" -c config/kraft/server.properties
bin/kafka-server-start.sh config/kraft/server.properties
cd - || exit
