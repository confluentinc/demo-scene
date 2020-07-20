kafka-producer-perf-test \
    --topic CLIENT_CURRENCY_UPDATES \
    --throughput 1 \
    --producer.config producer-java.properties \
    --payload-file ../data/client_currency_updates.json \
    --num-records 100000 &

echo "Don't forget to kill the producer when done"   

kafka-producer-perf-test \
    --topic CURRENCY_PAIRS_UPDATES \
    --throughput 5 \
    --producer.config producer-java.properties \
    --payload-file ../data/currency_pairs_updates.json \
    --num-records 100000 &

echo "Don't forget to kill the producer when done"   
