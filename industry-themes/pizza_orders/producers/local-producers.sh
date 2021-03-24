kafka-producer-perf-test \
    --topic pizza_orders \
    --throughput 1 \
    --producer.config local-producer.properties \
    --payload-file ../data/pizza_orders.json \
    --num-records 100000 &

export PID1=$!

kafka-producer-perf-test \
    --topic pizza_orders_cancelled \
    --throughput 1 \
    --producer.config local-producer.properties \
    --payload-file ../data/pizza_orders_cancelled.json \
    --num-records 100000 &

export PID2=$!

kafka-producer-perf-test \
    --topic pizza_orders_completed \
    --throughput 1 \
    --producer.config local-producer.properties \
    --payload-file ../data/pizza_orders_completed.json \
    --num-records 100000 &

export PID3=$!

echo "producers running don't forget to clean up after yourself!"
echo "kill -6 ${PID1} ${PID2} ${PID3}" > cleanup.sh