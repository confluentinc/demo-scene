# Configure the Confluent Provider
terraform {
  backend "local" {
    workspace_dir = ".tfstate/terraform.state"
  }

  required_providers {
    confluent = {
      source  = "confluentinc/confluent"
      version = "1.82.0"
    }
  }
}

provider "confluent" {
}

resource "confluent_environment" "cc_env" {
  display_name = var.cc_env_display_name

  lifecycle {
    prevent_destroy = false
  }
}
