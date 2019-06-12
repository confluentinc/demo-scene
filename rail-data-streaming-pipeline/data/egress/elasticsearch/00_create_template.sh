#!/usr/bin/env bash

curl -XDELETE localhost:9200/_template/kafkaconnect

curl -XPUT "http://localhost:9200/_template/kafkaconnect/" -H 'Content-Type: application/json' -d' {
  "index_patterns": "*",
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0
  },
  "mappings": {
    "_default_": {
      "dynamic_templates": [
        {
          "dates01": {
            "match": "*_TIMESTAMP",
            "mapping": {
              "type": "date",
              "ignore_malformed": true
            }
          }
        },
        {
          "numbers": {
            "match": "TIMETABLE_VARIATION",
            "mapping": {
              "type": "short",
              "ignore_malformed": true
            }
          }
        },
        {
          "non_analysed_string_template": {
            "match": "*",
            "match_mapping_type": "string",
            "mapping": {
              "type": "keyword"
            }
          }
        }
      ]
    }
  }
}'

