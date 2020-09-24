#!/bin/bash

echo -e "\n==> Using replica status to see mirrored topic"

docker-compose exec broker-east kafka-replica-status \
	--bootstrap-server=broker-east:19092 \
	--include-linked

echo -e "\n==> Stop west-link"

docker-compose exec broker-east kafka-topics --alter --mirror-action stop \
	--bootstrap-server=broker-east:19092 \
  --topic west-trades

echo -e "\n==> Monitor the change in mirrored topic status"

docker-compose exec broker-east kafka-replica-status \
	--bootstrap-server=broker-east:19092 \
	--include-linked
