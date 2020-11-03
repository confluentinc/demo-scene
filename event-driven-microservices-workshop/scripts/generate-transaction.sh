#!/bin/bash

ACCOUNT=$1
TYPE=$2
AMOUNT=$3
CURRENCY=$4

curl -X POST -H "Content-Type: application/json"  http://localhost:8080/transaction --data "{\"account\": \"$ACCOUNT\", \"amount\": $AMOUNT, \"type\": \"$TYPE\", \"currency\": \"$CURRENCY\"}"