kafka-topics --bootstrap-server pkc-43n10.us-central1.gcp.confluent.cloud:9092 \
    --command-config producer-java.properties \
    --create \
    --topic CLIENT_CURRENCY_UPDATES \
    --partitions 6 \
    --replication-factor 3 

kafka-topics --bootstrap-server pkc-43n10.us-central1.gcp.confluent.cloud:9092 \
    --command-config producer-java.properties \
    --create \
    --topic CURRENCY_PAIRS_UPDATES \
    --partitions 6 \
    --replication-factor 3