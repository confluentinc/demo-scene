#!/usr/bin/env bash

echo 'Indices and doc count'
echo '---------------------'
curl -s "http://localhost:9200/_cat/indices/train*"

echo ' '

echo 'Indices and timestamp of latest doc'
echo '-----------------------------------'
curl -s "http://localhost:9200/_cat/indices/train*?h=i" | \
                                                      xargs -Iix curl -s http://localhost:9200/{ix}/_search  -H 'Content-Type: application/json' -d '{ "query": { "match_all": {} }, "sort": [ { "HEADER.MSG_QUEUE_TIMESTAMP": { "order": "desc" } } ], "size": 1 }'| \
                                                      jq -c '[.hits.hits[]._index,.hits.hits[]._source.HEADER.MSG_QUEUE_TIMESTAMP]'| \
                                                      sed -e 's/\[//g' | \
                                                      sed -e 's/\]//g' | \
                                                      sed -e 's/"//g'| \
                                                      sed -e 's/000$//g' | \
                                                      awk -F , '{print strftime("%Y-%m-%d %T",$2) "\t\t" $1}'
