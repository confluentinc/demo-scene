#!/usr/bin/env bash

curl -s -XPUT "http://elasticsearch:9200/_template/kafkaconnect/" -H 'Content-Type: application/json' -d'
          {
            "template": "*",
            "settings": { "number_of_shards": 1, "number_of_replicas": 0 },
            "mappings": { "dynamic_templates": [ { "dates": { "match": "*_TS", "mapping": { "type": "date" } } } ]  }
          }'
