#!/bin/bash
cd "$(dirname "$0")"

echo "________Starting Databases________"
docker-compose up -d redis postgres

sleep 20

echo "________Migrate AWX Data________"
docker-compose run --rm --service-ports task awx-manage migrate --no-input

echo "________Start Everything________"
docker-compose up -d

sleep 20
