#!/usr/bin/env bash
docker exec -it schema-registry /usr/bin/kafka-avro-console-consumer --topic movies --bootstrap-server broker:9092 --from-beginning --property schema.registry.url=http://schema-registry:8081

