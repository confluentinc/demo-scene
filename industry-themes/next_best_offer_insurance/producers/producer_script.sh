cat ../data/customers_insurance.json | kafkacat -b localhost:9092 -t CUSTOMERS_STREAM
cat ../data/offers_insurance.json | kafkacat -b localhost:9092 -t OFFERS_STREAM
kafka-producer-perf-test \
    --topic CUSTOMER_ACTIVITY_STREAM \
    --throughput 1 \
    --producer-props bootstrap.servers=localhost:9092 \
    --payload-file ../data/customer_activity_insurance.json \
    --num-records 100000
