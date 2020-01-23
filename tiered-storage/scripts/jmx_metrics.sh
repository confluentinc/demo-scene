#!/bin/bash

value=$(docker-compose exec broker kafka-run-class kafka.tools.JmxTool --jmx-url service:jmx:rmi:///jndi/rmi://localhost:8091/jmxrmi --object-name kafka.server:type=TierFetcher --one-time true | tail -n 1 | awk -F, '{print $2;}' | head -c 1)
echo "Fetcher Throughput: $value bytes/second"
