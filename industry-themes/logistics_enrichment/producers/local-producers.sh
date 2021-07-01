cat ../data/parts.json | kafkacat -b localhost:9092 -t parts
kafka-producer-perf-test \
    --topic part_location \
    --throughput 1 \
    --producer-props bootstrap.servers=localhost:9092 \
    --payload-file ../data/part_location.json \
    --num-records 100000
