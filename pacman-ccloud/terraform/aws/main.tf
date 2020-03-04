###########################################
################## AWS ####################
###########################################

provider "aws" {
  access_key = var.aws_access_key
  secret_key = var.aws_secret_key
  region = local.region
}

data "aws_availability_zones" "available" {
  state = "available"
}

resource "random_string" "random_string" {
  length = 8
  special = false
  upper = false
  lower = true
  number = false
}

data "template_file" "bucket_pacman" {
  template = "pacman${random_string.random_string.result}"
}

resource "aws_s3_bucket" "pacman" {
  bucket = data.template_file.bucket_pacman.rendered
  acl = "public-read"
  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["GET", "POST"]
    allowed_origins = ["*"]
  }
  policy = <<EOF
{
    "Version": "2008-10-17",
    "Statement": [
        {
            "Sid": "PublicReadGetObject",
            "Effect": "Allow",
            "Principal": "*",
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::${data.template_file.bucket_pacman.rendered}/*"
        }
    ]
}
EOF
  website {
    index_document = "index.html"
    error_document = "error.html"
  }
}

###########################################
############## AWS Variables ##############
###########################################

variable "aws_access_key" {
  type = string
}

variable "aws_secret_key" {
  type = string
}

###########################################
############ CCloud Variables #############
###########################################

variable "bootstrap_server" {
  type = string
}

variable "cluster_api_key" {
  type = string
}

variable "cluster_api_secret" {
  type = string
}

variable "schema_registry_url" {
  type = string
}

variable "schema_registry_basic_auth" {
  type = string
}

locals {
  region = split(".", var.bootstrap_server)[1]
}

###########################################
############ Alexa Variables ##############
###########################################

variable "alexa_enabled" {
  type = bool
  default = false
}

variable "pacman_players_skill_id" {
  type = string
  default = ""
}

###########################################
############ Other Variables ##############
###########################################

variable "global_prefix" {
  type = string
  default = "pacman-ccloud"
}

variable "ksqldb_server_image" {
  type = string
  default = "confluentinc/ksqldb-server"
}

variable "redis_sink_image" {
  type = string
  default = "riferrei/redis-sink"
}

###########################################
############### Local Files ###############
###########################################

data "template_file" "ccloud_properties" {
  template = file("../../scoreboard/ccloud.template")
  vars = {
    bootstrap_server = var.bootstrap_server
    cluster_api_key = var.cluster_api_key
    cluster_api_secret = var.cluster_api_secret
    schema_registry_url = var.schema_registry_url
    schema_registry_username = split(":", var.schema_registry_basic_auth)[0]
    schema_registry_password = split(":", var.schema_registry_basic_auth)[1]
  }
}

resource "local_file" "ccloud_properties" {
  content  = data.template_file.ccloud_properties.rendered
  filename = "../../scoreboard/ccloud.properties"
}
