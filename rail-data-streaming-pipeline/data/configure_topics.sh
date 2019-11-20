#!/usr/bin/env bash

# 1000 * 60 * 60 * 24 * 7 * 26 
# 15724800000 = 26 weeks
# 15811200000 = 183 days

# Movement data
docker exec kafka /usr/bin/kafka-topics --zookeeper zookeeper:2181 --create --topic networkrail_TRAIN_MVT_X --replication-factor 1 --partitions 10

# Cancellation reasons
docker exec kafka /usr/bin/kafka-configs --zookeeper zookeeper:2181 --alter --entity-type topics --entity-name canx_reason_code --add-config retention.ms=15724800000

# Schedule / location data
docker exec kafka /usr/bin/kafka-configs --zookeeper zookeeper:2181 --alter --entity-type topics --entity-name CIF_FULL_DAILY --add-config retention.ms=15724800000
docker exec kafka /usr/bin/kafka-configs --zookeeper zookeeper:2181 --alter --entity-type topics --entity-name SCHEDULE_02 --add-config cleanup.policy=compact 
docker exec kafka /usr/bin/kafka-configs --zookeeper zookeeper:2181 --alter --entity-type topics --entity-name TIPLOC_FLAT_KEYED --add-config cleanup.policy=compact 
docker exec kafka /usr/bin/kafka-configs --zookeeper zookeeper:2181 --alter --entity-type topics --entity-name STANOX_FLAT_KEYED --add-config cleanup.policy=compact 

# Movement data
docker exec kafka /usr/bin/kafka-configs --zookeeper zookeeper:2181 --alter --entity-type topics --entity-name networkrail_TRAIN_MVT_X --add-config retention.ms=15724800000
docker exec kafka /usr/bin/kafka-configs --zookeeper zookeeper:2181 --alter --entity-type topics --entity-name networkrail_TRAIN_MVT --add-config retention.ms=15724800000

docker exec kafka /usr/bin/kafka-configs --zookeeper zookeeper:2181 --alter --entity-type topics --entity-name TRAIN_ACTIVATIONS_00 --add-config retention.ms=15724800000
docker exec kafka /usr/bin/kafka-configs --zookeeper zookeeper:2181 --alter --entity-type topics --entity-name TRAIN_ACTIVATIONS_01 --add-config retention.ms=15724800000
docker exec kafka /usr/bin/kafka-configs --zookeeper zookeeper:2181 --alter --entity-type topics --entity-name TRAIN_CANCELLATIONS_00 --add-config retention.ms=15724800000
docker exec kafka /usr/bin/kafka-configs --zookeeper zookeeper:2181 --alter --entity-type topics --entity-name TRAIN_CANCELLATIONS_01 --add-config retention.ms=15724800000
docker exec kafka /usr/bin/kafka-configs --zookeeper zookeeper:2181 --alter --entity-type topics --entity-name TRAIN_CANCELLATIONS_02 --add-config retention.ms=15724800000
docker exec kafka /usr/bin/kafka-configs --zookeeper zookeeper:2181 --alter --entity-type topics --entity-name TRAIN_CANCELLATIONS_ACTIVATIONS_00 --add-config retention.ms=15724800000
docker exec kafka /usr/bin/kafka-configs --zookeeper zookeeper:2181 --alter --entity-type topics --entity-name TRAIN_CANCELLATIONS_ACTIVATIONS_SCHEDULE_00 --add-config retention.ms=31449600000
docker exec kafka /usr/bin/kafka-configs --zookeeper zookeeper:2181 --alter --entity-type topics --entity-name TRAIN_MOVEMENTS_00 --add-config retention.ms=15724800000
docker exec kafka /usr/bin/kafka-configs --zookeeper zookeeper:2181 --alter --entity-type topics --entity-name TRAIN_MOVEMENTS_01 --add-config retention.ms=15724800000
docker exec kafka /usr/bin/kafka-configs --zookeeper zookeeper:2181 --alter --entity-type topics --entity-name TRAIN_MOVEMENTS_ACTIVATIONS_00 --add-config retention.ms=15724800000
docker exec kafka /usr/bin/kafka-configs --zookeeper zookeeper:2181 --alter --entity-type topics --entity-name TRAIN_MOVEMENTS_ACTIVATIONS_SCHEDULE_00 --add-config retention.ms=31449600000

# Derived data
docker exec kafka /usr/bin/kafka-configs --zookeeper zookeeper:2181 --alter --entity-type topics --entity-name TOC_VARIATION_STATS_DAILY --add-config retention.ms=15724800000

docker exec kafka /usr/bin/kafka-configs --zookeeper zookeeper:2181 --alter --entity-type topics --entity-name TRAINS_DELAYED_ALERT --add-config retention.ms=15724800000
docker exec kafka /usr/bin/kafka-configs --zookeeper zookeeper:2181 --alter --entity-type topics --entity-name TRAINS_DELAYED_ALERT_TG --add-config retention.ms=15724800000
