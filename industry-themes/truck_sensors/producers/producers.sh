kafka-producer-perf-test \
    --topic TRUCK_ENGINE_SENSORS \
    --throughput 5 \
    --producer-props bootstrap.servers=localhost:9092 \
    --payload-file ../data/truck_engine_sensors.json \
    --num-records 100000 &
kafka-producer-perf-test \
    --topic TRUCK_LOCATION \
    --throughput 5 \
    --producer-props bootstrap.servers=localhost:9092 \
    --payload-file ../data/truck_location.json \
    --num-records 100000 &