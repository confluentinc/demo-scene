#!/usr/bin/env bash

curl -s "http://localhost:9200/_cat/indices"|grep -v kibana
