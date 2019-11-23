locals {
  region = split(".", var.bootstrap_server)[1]
}

variable "instance_count" {
  type = map(string)
  default = {
    "ksql_server" = 1
  }
}

variable "ksql_server_image" {
  default = "confluentinc/cp-ksql-server"
}
