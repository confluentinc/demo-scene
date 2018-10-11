#!/bin/sh
curl -XPUT "http://elasticsearch:9200/_template/kafkaconnect/" -H 'Content-Type: application/json' -d'
{
  "index_patterns": "*atm*",
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0
  },
  "mappings": {
    "kafkaconnect": {
      "dynamic_templates": [
        {
          "dates": {
            "match": "*TIMESTAMP",
            "mapping": {
              "type": "date"
            }
          }
        },
        {
          "dates2": {
            "match": "timestamp",
            "mapping": {
              "type": "date",
              "format": "YYYY-MM-dd HH:mm:ss Z"

            }
          }
        },
        {
          "geopoint": {
            "match": "*LOCATION",
            "mapping": {
              "type": "geo_point"
            }
          }
        },
        {
          "geopoint2": {
            "match": "location",
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

