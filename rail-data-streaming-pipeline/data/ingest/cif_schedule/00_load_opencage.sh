#!/usr/bin/env bash

jq '. + {"rest_query": .request.query} | del(.request)' -c opencage.json|kafkacat -b localhost:9092 -t opencage -P
