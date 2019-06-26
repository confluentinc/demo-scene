#!/usr/bin/env bash
until kafkacat -b kafka:29092 -L | grep POOR_RATINGS
do
  sleep 1
done
npm start
