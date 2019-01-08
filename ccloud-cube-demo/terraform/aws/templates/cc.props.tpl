ssl.endpoint.identification.algorithm=https
sasl.mechanism=PLAIN
request.timeout.ms=20000
bootstrap.servers=${broker_list}
retry.backoff.ms=500

sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required \
    username="${access_key}" \
    password="${secret_key}";

security.protocol=SASL_SSL
