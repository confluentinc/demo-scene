
###########################################
############## AWS Variables ##############
###########################################

variable "aws_region" {
  type = string
}

variable "aws_profile" {
  type = string
}

###########################################
############ CCloud Variables #############
###########################################

variable "confluent_cloud_api_key" {
  description = "Confluent Cloud API Key (also referred as Cloud API ID)"
  type        = string
}

variable "confluent_cloud_api_secret" {
  description = "Confluent Cloud API Secret"
  type        = string
  sensitive   = true
}

variable "schema_registry_region" {
  description = "Confluent Cloud Schema Registry Region"
  type        = string
}


variable "scoreboard_topic" {
  type = string
  default = "SCOREBOARD"
}

###########################################
############ Alexa Variables ##############
###########################################

# variable "alexa_enabled" {
#   type = bool
#   default = false
# }

# variable "pacman_players_skill_id" {
#   type = string
#   default = ""
# }

###########################################
############ Other Variables ##############
###########################################

variable "global_prefix" {
  type = string
  default = "streaming-pacman"
}

variable "bucket_name" {
  type = string
  default = ""
}
