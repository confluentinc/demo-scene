kafka-producer-perf-test \
    --topic DEVICE_SENSORS \
    --throughput 20 \
    --producer-props bootstrap.servers=localhost:9092 \
    --payload-file ../data/sensor_data.json \
    --num-records 100000 &
