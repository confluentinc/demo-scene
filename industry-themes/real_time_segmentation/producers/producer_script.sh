cat ../data/customers_retail.json | kafkacat -b localhost:29092 -t CUSTOMERS_STREAM
cat ../data/products_grocery.json | kafkacat -b localhost:29092 -t PRODUCTS_STREAM
kafka-producer-perf-test \
    --topic CUSTOMER_ACTIVITY_STREAM \
    --throughput 1 \
    --producer-props bootstrap.servers=localhost:29092 \
    --payload-file ../data/customer_activity_retail.json \
    --num-records 100000
