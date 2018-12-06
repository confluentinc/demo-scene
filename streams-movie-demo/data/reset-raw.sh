#!/usr/bin/env bash
confluent destroy
confluent start schema-registry
cat movies.dat| kafka-console-producer --broker-list localhost:9092 --topic raw-movies
cat ratings.dat | kafka-console-producer --broker-list localhost:9092 --topic raw-ratings
# enable compaction for this topics
$CONFLUENT_HOME/bin/kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --config cleanup.policy=compact --topic movies
$CONFLUENT_HOME/bin/kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --config cleanup.policy=compact --topic rating-sums
$CONFLUENT_HOME/bin/kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --config cleanup.policy=compact --topic rating-counts
$CONFLUENT_HOME/bin/kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --config cleanup.policy=compact --topic rating-averages
$CONFLUENT_HOME/bin/kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --config cleanup.policy=compact --topic rated-movies