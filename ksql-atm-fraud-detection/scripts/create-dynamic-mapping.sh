#!/bin/sh
curl -XPUT "http://elasticsearch:9200/_template/kafkaconnect/" -H 'Content-Type: application/json' -d'
{
  "index_patterns": "*",
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0
  },
  "mappings": {
    "_default_": {
      "dynamic_templates": [
        {
          "dates": {
            "match": "*TIME",
            "mapping": {
              "type": "date"
            }
          }
        },
        {
          "geopoint": {
            "match": "location, TXN1_LOCATION, TXN2_LOCATION",
            "mapping": {
              "type": "geo_point"
            }
          }
        },
        {
          "non_analysed_string_template": {
            "match": "account_id, atm, transaction_id",
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

