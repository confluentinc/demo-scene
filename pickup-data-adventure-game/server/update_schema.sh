#!/bin/sh

mkdir -p src/main/avro

wget -qO - http://localhost:8081/subjects/commands-value/versions/latest \
		| jq -rc .schema \
		| jq '.' \
		| tee src/main/avro/commands-value.avsc

wget -qO - http://localhost:8081/subjects/location_data-value/versions/latest \
		| jq -rc .schema \
		| jq '.' \
		| tee src/main/avro/location_data-value.avsc

wget -qO - http://localhost:8081/subjects/item_rules-value/versions/latest \
		| jq -rc .schema \
		| jq '.' \
		| tee src/main/avro/item_rules-value.avsc

wget -qO - http://localhost:8081/subjects/status_commands-value/versions/latest \
		| jq -rc .schema \
		| jq '.' \
		| tee src/main/avro/status_command-value.avsc

wget -qO - http://localhost:8081/subjects/movement_commands-value/versions/latest \
		| jq -rc .schema \
		| jq '.' \
		| tee src/main/avro/movement_command-value.avsc

wget -qO - http://localhost:8081/subjects/inventory_commands-value/versions/latest \
		| jq -rc .schema \
		| jq '.' \
		| tee src/main/avro/inventory_command-value.avsc

wget -qO - http://localhost:8081/subjects/inventory-value/versions/latest \
		| jq -rc .schema \
		| jq '.' \
		| tee src/main/avro/inventory-value.avsc

wget -qO - http://localhost:8081/subjects/responses-value/versions/latest \
		| jq -rc .schema \
		| jq '.' \
		| tee src/main/avro/responses-value.avsc
