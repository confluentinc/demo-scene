resource "confluent_kafka_topic" "wc-input-topic" {
  kafka_cluster {
    id = confluent_kafka_cluster.kafka_cluster.id
  }

  topic_name = "wc-input-topic"
  rest_endpoint = confluent_kafka_cluster.kafka_cluster.rest_endpoint

  credentials {
    key    = confluent_api_key.app-manager-kafka-api-key.id
    secret = confluent_api_key.app-manager-kafka-api-key.secret
  }

  partitions_count = 3

  config = {
    "cleanup.policy" = "delete"
  }

  lifecycle {
    prevent_destroy = false
  }

  depends_on = [
    confluent_api_key.app-manager-kafka-api-key,
    confluent_api_key.env-manager-schema-registry-api-key,
    confluent_role_binding.app-manager-kafka-cluster-admin,
    confluent_role_binding.env-manager-environment-admin
  ]
}

resource "confluent_kafka_topic" "wc-output-topic" {
  kafka_cluster {
    id = confluent_kafka_cluster.kafka_cluster.id
  }

  topic_name = "wc-output-topic"
  rest_endpoint = confluent_kafka_cluster.kafka_cluster.rest_endpoint

  credentials {
    key    = confluent_api_key.app-manager-kafka-api-key.id
    secret = confluent_api_key.app-manager-kafka-api-key.secret
  }

  partitions_count = 3

  config = {
    "cleanup.policy" = "delete"
  }

  lifecycle {
    prevent_destroy = false
  }

  depends_on = [
    confluent_api_key.app-manager-kafka-api-key,
    confluent_api_key.env-manager-schema-registry-api-key,
    confluent_role_binding.app-manager-kafka-cluster-admin,
    confluent_role_binding.env-manager-environment-admin
  ]
}