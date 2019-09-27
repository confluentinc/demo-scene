###########################################
################## GCP ####################
###########################################

provider "google" {
  credentials = file(var.gcp_credentials)
  project     = var.gcp_project
  region      = local.region
}

variable "gcp_credentials" {
}

variable "gcp_project" {
}

###########################################
############# Confluent Cloud #############
###########################################

variable "bootstrap_server" {
}

variable "cluster_api_key" {
}

variable "cluster_api_secret" {
}

variable "schema_registry_url" {
}

variable "schema_registry_basic_auth" {
}

###########################################
################## Others #################
###########################################

variable "global_prefix" {
  default = "pacman-ccloud"
}
