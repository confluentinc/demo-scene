cat ../data/products_ppe.json | kafkacat -b localhost:9092 -t PRODUCTS_PPE
cat ../data/contract_price.json | kafkacat -b localhost:9092 -t CONTRACT_PRICE
kafka-producer-perf-test \
    --topic CUSTOMER_PAGE_VIEWS \
    --throughput 5 \
    --producer-props bootstrap.servers=localhost:9092 \
    --payload-file ../data/customer_page_views.json \
    --num-records 100000
