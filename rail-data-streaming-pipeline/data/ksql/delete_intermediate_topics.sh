#!/usr/bin/env bash

docker-compose exec kafka kafka-topics --zookeeper zookeeper:2181 --delete --topic canx_reason_code2
docker-compose exec kafka kafka-topics --zookeeper zookeeper:2181 --delete --topic CORPUS_BY_STANOX
docker-compose exec kafka kafka-topics --zookeeper zookeeper:2181 --delete --topic networkrail_TRAIN_MVT
docker-compose exec kafka kafka-topics --zookeeper zookeeper:2181 --delete --topic schedule
docker-compose exec kafka kafka-topics --zookeeper zookeeper:2181 --delete --topic SCHEDULE_00
docker-compose exec kafka kafka-topics --zookeeper zookeeper:2181 --delete --topic SCHEDULE_01
docker-compose exec kafka kafka-topics --zookeeper zookeeper:2181 --delete --topic TIPLOC_FLAT_KEYED
docker-compose exec kafka kafka-topics --zookeeper zookeeper:2181 --delete --topic TRAIN_ACTIVATIONS_00
docker-compose exec kafka kafka-topics --zookeeper zookeeper:2181 --delete --topic TRAIN_ACTIVATIONS_01
docker-compose exec kafka kafka-topics --zookeeper zookeeper:2181 --delete --topic TRAIN_CANCELLATIONS_00
docker-compose exec kafka kafka-topics --zookeeper zookeeper:2181 --delete --topic TRAIN_CANCELLATIONS_01
docker-compose exec kafka kafka-topics --zookeeper zookeeper:2181 --delete --topic TRAIN_CANCELLATIONS_02
docker-compose exec kafka kafka-topics --zookeeper zookeeper:2181 --delete --topic TRAIN_MOVEMENTS_00
docker-compose exec kafka kafka-topics --zookeeper zookeeper:2181 --delete --topic TRAIN_MOVEMENTS_00A
docker-compose exec kafka kafka-topics --zookeeper zookeeper:2181 --delete --topic TRAIN_MOVEMENTS_01
docker-compose exec kafka kafka-topics --zookeeper zookeeper:2181 --delete --topic TRAIN_MOVEMENTS_02
docker-compose exec kafka kafka-topics --zookeeper zookeeper:2181 --delete --topic TRAIN_MOVEMENTS_ACTIVATIONS_00
docker-compose exec kafka kafka-topics --zookeeper zookeeper:2181 --delete --topic TRAIN_MOVEMENTS_ACTIVATIONS_SCHEDULE_00
docker-compose exec kafka kafka-topics --zookeeper zookeeper:2181 --delete --topic TRAIN_MOVEMENTS_ACTIVATIONS_SCHEDULE_01
