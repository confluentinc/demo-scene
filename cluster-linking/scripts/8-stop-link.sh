#!/bin/bash

echo -e "\n==> Using replica status to see mirrored topic"

docker-compose exec broker-east kafka-mirrors --describe \
        --topics west-trades \
        --pending-stopped-only \
        --bootstrap-server=broker-east:19092

echo -e "\n==> Stop west-link"

docker-compose exec broker-east kafka-mirrors --promote \
	--topics west-trades \
	--bootstrap-server=broker-east:19092

echo -e "\n==> Monitor the change in mirrored topic status"

docker-compose exec broker-east kafka-mirrors --describe \
	--topics west-trades \
        --pending-stopped-only \
	--bootstrap-server=broker-east:19092
