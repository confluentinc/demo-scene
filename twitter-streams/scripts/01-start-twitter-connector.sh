#!/bin/bash

# init connector
curl -s -X POST -H 'Content-Type: application/json' --data @connect_twitter.json http://localhost:8083/connectors