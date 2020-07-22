cat ../data/patients.json | kafkacat -b localhost:29092 -t PATIENTS_STREAM
kafka-producer-perf-test \
    --topic CLAIMS_STREAM \
    --throughput 1 \
    --producer-props bootstrap.servers=localhost:29092 \
    --payload-file ../data/fhir_claims.json \
    --num-records 100000
