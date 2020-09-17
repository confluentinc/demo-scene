kafka-producer-perf-test \
    --topic UTILITY_WORK_ORDERS \
    --throughput 1 \
    --producer.config cloud-producer.properties \
    --payload-file ../data/utility_work_orders.json \
    --num-records 100000 &

echo "Don't forget to kill the producer when done"

kafka-producer-perf-test \
    --topic UTILITY_TRUCK_LOCATION \
    --throughput 5 \
    --producer.config cloud-producer.properties \
    --payload-file ../data/utility_truck_location.json \
    --num-records 100000 &

echo "Don't forget to kill the producer when done"