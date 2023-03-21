terraform {
  required_providers {
    confluent = {
      source  = "confluentinc/confluent"
      version = "1.35.0"
    }
  }
}

locals {
  ksql_basic_auth_user_info= "${confluent_api_key.app-ksqldb-api-key.id}:${confluent_api_key.app-ksqldb-api-key.secret}"
}

provider "confluent" {
  cloud_api_key    = var.confluent_cloud_api_key
  cloud_api_secret = var.confluent_cloud_api_secret
}

resource "confluent_environment" "staging" {
  display_name = local.resource_prefix
}

data "confluent_schema_registry_region" "essentials" {
  cloud   = "AWS"
  region  = var.schema_registry_region
  package = "ESSENTIALS"
}

resource "confluent_schema_registry_cluster" "essentials" {
  package = data.confluent_schema_registry_region.essentials.package

  environment {
    id = confluent_environment.staging.id
  }

  region {
    # See https://docs.confluent.io/cloud/current/stream-governance/packages.html#stream-governance-regions
    # Stream Governance and Kafka clusters can be in different regions as well as different cloud providers,
    # but you should to place both in the same cloud and region to restrict the fault isolation boundary.
    id = data.confluent_schema_registry_region.essentials.id
  }
}

# Update the config to use a cloud provider and region of your choice.
# https://registry.terraform.io/providers/confluentinc/confluent/latest/docs/resources/confluent_kafka_cluster
resource "confluent_kafka_cluster" "pacman-demo" {
  display_name = "inventory"
  availability = "SINGLE_ZONE"
  cloud        = "AWS"
  region       = var.aws_region
  basic {}
  environment {
    id = confluent_environment.staging.id
  }
}

// 'app-manager' service account is required in this configuration to grant ACLs
// to 'app-ksql' service account and create 'USER_GAME' and 'USER_LOSSES' topics
resource "confluent_service_account" "app-manager" {
  display_name = "app-manager"
  description  = "Service account to manage 'inventory' Kafka cluster"
}

resource "confluent_role_binding" "app-manager-kafka-cluster-admin" {
  principal   = "User:${confluent_service_account.app-manager.id}"
  role_name   = "CloudClusterAdmin"
  crn_pattern = confluent_kafka_cluster.pacman-demo.rbac_crn
}

resource "confluent_api_key" "app-manager-kafka-api-key" {
  display_name = "app-manager-kafka-api-key"
  description  = "Kafka API Key that is owned by 'app-manager' service account"
  owner {
    id          = confluent_service_account.app-manager.id
    api_version = confluent_service_account.app-manager.api_version
    kind        = confluent_service_account.app-manager.kind
  }

  managed_resource {
    id          = confluent_kafka_cluster.pacman-demo.id
    api_version = confluent_kafka_cluster.pacman-demo.api_version
    kind        = confluent_kafka_cluster.pacman-demo.kind

    environment {
      id = confluent_environment.staging.id
    }
  }

  # The goal is to ensure that confluent_role_binding.app-manager-kafka-cluster-admin is created before
  # confluent_api_key.app-manager-kafka-api-key is used to create instances of
  # confluent_kafka_topic, confluent_kafka_acl resources.

  # 'depends_on' meta-argument is specified in confluent_api_key.app-manager-kafka-api-key to avoid having
  # multiple copies of this definition in the configuration which would happen if we specify it in
  # confluent_kafka_topic, confluent_kafka_acl resources instead.
  depends_on = [
    confluent_role_binding.app-manager-kafka-cluster-admin
  ]
}

resource "confluent_kafka_topic" "user_game" {
  kafka_cluster {
    id = confluent_kafka_cluster.pacman-demo.id
  }
  topic_name    = "USER_GAME"
  rest_endpoint = confluent_kafka_cluster.pacman-demo.rest_endpoint
  credentials {
    key    = confluent_api_key.app-manager-kafka-api-key.id
    secret = confluent_api_key.app-manager-kafka-api-key.secret
  }
}

resource "confluent_kafka_topic" "user_losses" {
  kafka_cluster {
    id = confluent_kafka_cluster.pacman-demo.id
  }
  topic_name    = "USER_LOSSES"
  rest_endpoint = confluent_kafka_cluster.pacman-demo.rest_endpoint
  credentials {
    key    = confluent_api_key.app-manager-kafka-api-key.id
    secret = confluent_api_key.app-manager-kafka-api-key.secret
  }
}

// ksqlDB service account with only the necessary access
resource "confluent_service_account" "app-ksql" {
  display_name = "app-ksql"
  description  = "Service account for Ksql cluster"
}

resource "confluent_ksql_cluster" "main" {
  display_name = "ksql_cluster_0"
  csu = 1
  kafka_cluster {
    id = confluent_kafka_cluster.pacman-demo.id
  }
  credential_identity {
    id = confluent_service_account.app-ksql.id
  }
  environment {
    id = confluent_environment.staging.id
  }

  depends_on = [
    confluent_schema_registry_cluster.essentials,
    confluent_role_binding.app-ksql-schema-registry-resource-owner
  ]
}

resource "confluent_kafka_acl" "app-ksql-describe-on-cluster" {
  kafka_cluster {
    id = confluent_kafka_cluster.pacman-demo.id
  }
  resource_type = "CLUSTER"
  resource_name = "kafka-cluster"
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.app-ksql.id}"
  host          = "*"
  operation     = "DESCRIBE"
  permission    = "ALLOW"
  rest_endpoint = confluent_kafka_cluster.pacman-demo.rest_endpoint
  credentials {
    key    = confluent_api_key.app-manager-kafka-api-key.id
    secret = confluent_api_key.app-manager-kafka-api-key.secret
  }
}

resource "confluent_kafka_acl" "app-ksql-describe-on-topic" {
  kafka_cluster {
    id = confluent_kafka_cluster.pacman-demo.id
  }
  resource_type = "TOPIC"
  resource_name = "*"
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.app-ksql.id}"
  host          = "*"
  operation     = "DESCRIBE"
  permission    = "ALLOW"
  rest_endpoint = confluent_kafka_cluster.pacman-demo.rest_endpoint
  credentials {
    key    = confluent_api_key.app-manager-kafka-api-key.id
    secret = confluent_api_key.app-manager-kafka-api-key.secret
  }
}

resource "confluent_kafka_acl" "app-ksql-describe-on-group" {
  kafka_cluster {
    id = confluent_kafka_cluster.pacman-demo.id
  }
  resource_type = "GROUP"
  resource_name = "*"
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.app-ksql.id}"
  host          = "*"
  operation     = "DESCRIBE"
  permission    = "ALLOW"
  rest_endpoint = confluent_kafka_cluster.pacman-demo.rest_endpoint
  credentials {
    key    = confluent_api_key.app-manager-kafka-api-key.id
    secret = confluent_api_key.app-manager-kafka-api-key.secret
  }
}

resource "confluent_kafka_acl" "app-ksql-describe-configs-on-cluster" {
  kafka_cluster {
    id = confluent_kafka_cluster.pacman-demo.id
  }
  resource_type = "CLUSTER"
  resource_name = "kafka-cluster"
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.app-ksql.id}"
  host          = "*"
  operation     = "DESCRIBE_CONFIGS"
  permission    = "ALLOW"
  rest_endpoint = confluent_kafka_cluster.pacman-demo.rest_endpoint
  credentials {
    key    = confluent_api_key.app-manager-kafka-api-key.id
    secret = confluent_api_key.app-manager-kafka-api-key.secret
  }
}

resource "confluent_kafka_acl" "app-ksql-describe-configs-on-topic" {
  kafka_cluster {
    id = confluent_kafka_cluster.pacman-demo.id
  }
  resource_type = "TOPIC"
  resource_name = "*"
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.app-ksql.id}"
  host          = "*"
  operation     = "DESCRIBE_CONFIGS"
  permission    = "ALLOW"
  rest_endpoint = confluent_kafka_cluster.pacman-demo.rest_endpoint
  credentials {
    key    = confluent_api_key.app-manager-kafka-api-key.id
    secret = confluent_api_key.app-manager-kafka-api-key.secret
  }
}

resource "confluent_kafka_acl" "app-ksql-describe-configs-on-group" {
  kafka_cluster {
    id = confluent_kafka_cluster.pacman-demo.id
  }
  resource_type = "GROUP"
  resource_name = "*"
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.app-ksql.id}"
  host          = "*"
  operation     = "DESCRIBE_CONFIGS"
  permission    = "ALLOW"
  rest_endpoint = confluent_kafka_cluster.pacman-demo.rest_endpoint
  credentials {
    key    = confluent_api_key.app-manager-kafka-api-key.id
    secret = confluent_api_key.app-manager-kafka-api-key.secret
  }
}

resource "confluent_kafka_acl" "app-ksql-describe-on-transactional-id" {
  kafka_cluster {
    id = confluent_kafka_cluster.pacman-demo.id
  }
  resource_type = "TRANSACTIONAL_ID"
  resource_name = confluent_ksql_cluster.main.topic_prefix
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.app-ksql.id}"
  host          = "*"
  operation     = "DESCRIBE"
  permission    = "ALLOW"
  rest_endpoint = confluent_kafka_cluster.pacman-demo.rest_endpoint
  credentials {
    key    = confluent_api_key.app-manager-kafka-api-key.id
    secret = confluent_api_key.app-manager-kafka-api-key.secret
  }
}

resource "confluent_kafka_acl" "app-ksql-write-on-transactional-id" {
  kafka_cluster {
    id = confluent_kafka_cluster.pacman-demo.id
  }
  resource_type = "TRANSACTIONAL_ID"
  resource_name = confluent_ksql_cluster.main.topic_prefix
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.app-ksql.id}"
  host          = "*"
  operation     = "WRITE"
  permission    = "ALLOW"
  rest_endpoint = confluent_kafka_cluster.pacman-demo.rest_endpoint
  credentials {
    key    = confluent_api_key.app-manager-kafka-api-key.id
    secret = confluent_api_key.app-manager-kafka-api-key.secret
  }
}

resource "confluent_kafka_acl" "app-ksql-all-on-topic-prefix" {
  kafka_cluster {
    id = confluent_kafka_cluster.pacman-demo.id
  }
  resource_type = "TOPIC"
  resource_name = confluent_ksql_cluster.main.topic_prefix
  pattern_type  = "PREFIXED"
  principal     = "User:${confluent_service_account.app-ksql.id}"
  host          = "*"
  operation     = "ALL"
  permission    = "ALLOW"
  rest_endpoint = confluent_kafka_cluster.pacman-demo.rest_endpoint
  credentials {
    key    = confluent_api_key.app-manager-kafka-api-key.id
    secret = confluent_api_key.app-manager-kafka-api-key.secret
  }
}

resource "confluent_kafka_acl" "app-ksql-all-on-topic-confluent" {
  kafka_cluster {
    id = confluent_kafka_cluster.pacman-demo.id
  }
  resource_type = "TOPIC"
  resource_name = "_confluent-ksql-${confluent_ksql_cluster.main.topic_prefix}"
  pattern_type  = "PREFIXED"
  principal     = "User:${confluent_service_account.app-ksql.id}"
  host          = "*"
  operation     = "ALL"
  permission    = "ALLOW"
  rest_endpoint = confluent_kafka_cluster.pacman-demo.rest_endpoint
  credentials {
    key    = confluent_api_key.app-manager-kafka-api-key.id
    secret = confluent_api_key.app-manager-kafka-api-key.secret
  }
}

resource "confluent_kafka_acl" "app-ksql-all-on-group-confluent" {
  kafka_cluster {
    id = confluent_kafka_cluster.pacman-demo.id
  }
  resource_type = "GROUP"
  resource_name = "_confluent-ksql-${confluent_ksql_cluster.main.topic_prefix}"
  pattern_type  = "PREFIXED"
  principal     = "User:${confluent_service_account.app-ksql.id}"
  host          = "*"
  operation     = "ALL"
  permission    = "ALLOW"
  rest_endpoint = confluent_kafka_cluster.pacman-demo.rest_endpoint
  credentials {
    key    = confluent_api_key.app-manager-kafka-api-key.id
    secret = confluent_api_key.app-manager-kafka-api-key.secret
  }
}

# Topic specific permissions. You have to add an ACL like this for every Kafka topic you work with.
resource "confluent_kafka_acl" "app-ksql-all-on-topic-ug" {
  kafka_cluster {
    id = confluent_kafka_cluster.pacman-demo.id
  }
  resource_type = "TOPIC"
  resource_name = confluent_kafka_topic.user_game.topic_name
  pattern_type  = "PREFIXED"
  principal     = "User:${confluent_service_account.app-ksql.id}"
  host          = "*"
  operation     = "ALL"
  permission    = "ALLOW"
  rest_endpoint = confluent_kafka_cluster.pacman-demo.rest_endpoint
  credentials {
    key    = confluent_api_key.app-manager-kafka-api-key.id
    secret = confluent_api_key.app-manager-kafka-api-key.secret
  }
}

resource "confluent_kafka_acl" "app-ksql-all-on-topic-us" {
  kafka_cluster {
    id = confluent_kafka_cluster.pacman-demo.id
  }
  resource_type = "TOPIC"
  resource_name = confluent_kafka_topic.user_losses.topic_name
  pattern_type  = "PREFIXED"
  principal     = "User:${confluent_service_account.app-ksql.id}"
  host          = "*"
  operation     = "ALL"
  permission    = "ALLOW"
  rest_endpoint = confluent_kafka_cluster.pacman-demo.rest_endpoint
  credentials {
    key    = confluent_api_key.app-manager-kafka-api-key.id
    secret = confluent_api_key.app-manager-kafka-api-key.secret
  }
}

resource "confluent_role_binding" "app-ksql-schema-registry-resource-owner" {
  principal   = "User:${confluent_service_account.app-ksql.id}"
  role_name   = "ResourceOwner"
  crn_pattern = format("%s/%s", confluent_schema_registry_cluster.essentials.resource_name, "subject=*")
}

# ACLs are needed for KSQL service account to read/write data from/to kafka, this role instead is needed for giving
# access to the Ksql cluster.
resource "confluent_role_binding" "app-ksql-ksql-admin" {
  principal   = "User:${confluent_service_account.app-ksql.id}"
  #role_name   = "KsqlAdmin"
  role_name   = "CloudClusterAdmin"
  crn_pattern = confluent_kafka_cluster.pacman-demo.rbac_crn
}

resource "confluent_api_key" "app-ksqldb-api-key" {
  display_name = "app-ksqldb-api-key"
  description  = "KsqlDB API Key that is owned by 'app-ksql' service account"
  owner {
    id          = confluent_service_account.app-ksql.id
    api_version = confluent_service_account.app-ksql.api_version
    kind        = confluent_service_account.app-ksql.kind
  }

  managed_resource {
    id          = confluent_ksql_cluster.main.id
    api_version = confluent_ksql_cluster.main.api_version
    kind        = confluent_ksql_cluster.main.kind

    environment {
      id = confluent_environment.staging.id
    }
  }
}