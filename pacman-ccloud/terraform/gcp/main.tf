###########################################
################## GCP ####################
###########################################

provider "google" {
  credentials = file(var.gcp_credentials)
  project     = var.gcp_project
  region      = local.region
}

data "google_compute_zones" "available" {
  status = "UP"
}

resource "random_string" "random_string" {
  length  = 8
  special = false
  upper = false
  lower = true
  number = false
}

data "template_file" "storage_bucket_pacman" {
  template = "pacman${random_string.random_string.result}"
}

resource "google_storage_bucket" "pacman" {
  depends_on = ["google_compute_global_address.rest_proxy"]
  name = data.template_file.storage_bucket_pacman.rendered
  location = "us"
  project = var.gcp_project
  storage_class = "MULTI_REGIONAL"
  website {
      main_page_suffix = "index.html"
      not_found_page = "error.html"
  }
  force_destroy = "true"
}

resource "google_storage_default_object_acl" "default_obj_acl" {
  depends_on = ["google_storage_bucket.pacman"]
  bucket = "${google_storage_bucket.pacman.name}"
  role_entity = ["READER:AllUsers"]
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
