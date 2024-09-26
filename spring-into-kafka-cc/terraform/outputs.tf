output "CC_ENV_DISPLAY_NAME" {
  value = confluent_environment.cc_env.display_name
}

output "CC_ENV_ID" {
  value = confluent_environment.cc_env.id
}

output "CC_KAFKA_CLUSTER_ID" {
  value = confluent_kafka_cluster.kafka_cluster.id
}

output "CC_BROKER" {
  value = replace(confluent_kafka_cluster.kafka_cluster.bootstrap_endpoint, "SASL_SSL://", "")
}

output "CC_BROKER_URL" {
  value = confluent_kafka_cluster.kafka_cluster.rest_endpoint
}

output "CC_SCHEMA_REGISTRY_ID" {
  value = confluent_schema_registry_cluster.schema_registry_cluster.id
}

output "CC_SCHEMA_REGISTRY_URL" {
  value = confluent_schema_registry_cluster.schema_registry_cluster.rest_endpoint
}

output "SCHEMA_REGISTRY_KEY_ID" {
  value = confluent_api_key.env-manager-schema-registry-api-key.id
  sensitive = false
}

output "SCHEMA_REGISTRY_KEY_SECRET" {
  value = nonsensitive(confluent_api_key.env-manager-schema-registry-api-key.secret)
  #  sensitive = false
}

output "KAFKA_KEY_ID" {
  value = confluent_api_key.app-manager-kafka-api-key.id
  sensitive = false
}

output "KAFKA_KEY_SECRET" {
  value = nonsensitive(confluent_api_key.app-manager-kafka-api-key.secret)
  #  sensitive = false
}


output "customer_commands_topic_name" {
  value = confluent_kafka_topic.customer_commands.topic_name
}

output "customer_commands_value_subject_compatibility_level" {
  value = confluent_subject_config.customer_commands_value_cfg.compatibility_level
}

output "KAFKA_SASL_JAAS_CONFIG" {
  value = "org.apache.kafka.common.security.plain.PlainLoginModule required username='${confluent_api_key.app-manager-kafka-api-key.id}' password='${nonsensitive(confluent_api_key.app-manager-kafka-api-key.secret)}';"
}

output "SR_BASIC_AUTH_USER_INFO" {
  value = "${confluent_api_key.env-manager-schema-registry-api-key.id}:${nonsensitive(confluent_api_key.env-manager-schema-registry-api-key.secret)}"
}
