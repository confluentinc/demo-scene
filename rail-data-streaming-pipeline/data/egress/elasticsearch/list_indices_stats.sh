#!/usr/bin/env bash

echo 'Connectors'
echo '----------'
curl -s "http://localhost:28083/connectors"| jq '.[]'| xargs -I{connector_name} curl -s "http://localhost:28083/connectors/"{connector_name}"/status"| jq -c -M '[.name,.connector.state,.tasks[].state]|join(":|:")'| column -s : -t| sed 's/\"//g'| sort

echo ' '
echo 'Indices and doc count'
echo '---------------------'
curl -s "http://localhost:9200/_cat/indices/train*?h=i,dc"

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
