kafka-producer-perf-test \
    --topic auto_insurance_claims \
    --throughput 1 \
    --producer.config java-producer.properties \
    --payload-file ../data/auto_insurance_claims.json \
    --num-records 100000