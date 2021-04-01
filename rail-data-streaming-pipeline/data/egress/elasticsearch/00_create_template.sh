#!/usr/bin/env bash


curl -XPUT "http://localhost:9200/_index_template/rmoff_train_01/" \
     -H 'Content-Type: application/json' \
      -d'{  "index_patterns": [    "train*","schedule","locations_by_stanox"  ], 
             "template": {    "mappings": {      "dynamic_templates": [       
                { "locations": { "match": "*_LAT_LON", "mapping": { "type": "geo_point",                    "ignore_malformed": true } } },    
                { "numbers": { "match": "TIMETABLE_VARIATION", "mapping": { "type": "short", "ignore_malformed": true } } }, 
                { "non_analysed_string_template": { "match": "*", "match_mapping_type": "string", "mapping": { "type": "keyword" } } },    
                { "dates": { "match": "*_TIMESTAMP", "mapping": { "type": "date" } } }      
              ]    }  }}'