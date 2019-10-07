#!/bin/bash

source .env

curl -u $ELASTIC_USERNAME:$ELASTIC_PASSWORD -XPUT "$ELASTIC_URL/_template/kafkaconnect" -H 'Content-Type: application/json' -d'
{
            "template": "*",
            "settings": {
                "number_of_shards": 1,
                "number_of_replicas": 0
            },
            "mappings": {
                "dynamic_templates": [
                    {
                        "dates": {
                            "match": "*_TS",
                            "mapping": {
                                "type": "date"
                            }
                        }
                    },
                    {
                        "heights": {
                            "match": "HEIGHT",
                            "mapping": {
                                "type": "float"
                            }
                        }
                    },
                    {
                        "locations": {
                            "match": "LOCATION",
                            "mapping": {
                                "type": "geo_point"
                            }
                        }
                    }
                ]
            }
        }'