#!/bin/bash

if [ -z "$1" ]
  then
    echo "Usage: delete-topic.sh TOPIC_NAME"
    exit 1
fi
echo "Deliting topic $1 ..."
docker exec twitter-streams_connect_1 kafka-topics --zookeeper zookeeper:2181 --delete --topic $1
docker exec twitter-streams_connect_1 kafka-topics --zookeeper zookeeper:2181 --list
