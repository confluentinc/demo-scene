#!/bin/bash

docker-compose exec ksql-cli  bash -c "ksql http://ksql-server:8088 <<EOF
run script '/tmp/ksqlcommands';
exit ;
EOF
"