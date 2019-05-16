cat customers_insurance.json | kafkacat -b localhost:9092 -t CUSTOMERS_STREAM
cat offers_insurance.json | kafkacat -b localhost:9092 -t OFFERS_STREAM
kafka-producer-perf-test \
    --topic CUSTOMER_ACTIVITY_STREAM \
    --throughput 1 \
    --producer-props bootstrap.servers=localhost:9092 \
    --payload-file customer_activity_insurance.json \
    --num-records 100000