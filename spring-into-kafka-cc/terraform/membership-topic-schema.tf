resource "confluent_kafka_topic" "membership" {
  kafka_cluster {
    id = confluent_kafka_cluster.kafka_cluster.id
  }

  topic_name = "membership-avro"
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

resource "confluent_subject_config" "membership_value_cfg" {
  subject_name = "${confluent_kafka_topic.membership.topic_name}-value"
  compatibility_level = "BACKWARD"

  schema_registry_cluster {
    id = confluent_schema_registry_cluster.schema_registry_cluster.id
  }
  rest_endpoint = confluent_schema_registry_cluster.schema_registry_cluster.rest_endpoint

  credentials {
    key    = confluent_api_key.env-manager-schema-registry-api-key.id
    secret = confluent_api_key.env-manager-schema-registry-api-key.secret
  }

  lifecycle {
    prevent_destroy = false
  }

  depends_on = [
    confluent_kafka_topic.membership
  ]
}