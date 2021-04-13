variable "vpc_id" {
  description = "VPC ID"
  default     = "TODO"
}

variable "subnet" {
  description = "VPC Subnet ID the instance is launched in"
  default     = "TODO"
}

variable "region" {
  description = "AWS Region the instance is launched in"
  default     = "us-west-2"
}

variable "unique_identifier" {
  description = "Unique ID for AWS objects to avoid naming collisions"
  default = "confluent"
}

## Set this var to existing key pair if using existing one and comment next resource
variable "ssh_key_pair" {
  description = "SSH key pair to be provisioned on the instance"
  default = "cp-tower-key"
}

## Comment out this resource if keypair already exists
resource "aws_key_pair" "default" {
  key_name   = var.ssh_key_pair
  public_key = file(var.ssh_key_public_path)
}

variable "ssh_key_public_path" {
  description = "Path to local public ssh key"
  default = "id_rsa.pub"
}

variable "instance_type" {
  description = "The type of the instance"
  default     = "t2.large"
}

variable "ami" {
  description = "The AMI to use for the instance."
  # Below amis only work in us-west-2, if you select another region, this MUST be changed with it
  default = "ami-01ed306a12b7d1c96" # centos
  # default = "ami-0b37e9efc396e4c38" #Ubuntu 16
  # default = "ami-0b86e06624ac20c42" # Debian stretch
}

variable "zookeeper_instance_count" {
  description = "EC2 instance count of Zookeeper Nodes"
  default     = "3"
}

variable "kafka_instance_count" {
  description = "EC2 instance count of Kafka Brokers"
  default     = "3"
}

variable "schema_registry_instance_count" {
  description = "EC2 instance count of Schema Registry instances"
  default     = "1"
}

variable "connect_instance_count" {
  description = "EC2 instance count of Connect Nodes"
  default     = "1"
}

variable "rest_proxy_instance_count" {
  description = "EC2 instance count of Rest Proxy Nodes"
  default     = "1"
}

variable "ksql_instance_count" {
  description = "EC2 instance count of KSQL Nodes"
  default     = "1"
}

variable "control_center_instance_count" {
  description = "EC2 instance count of Control Center Nodes"
  default     = "1"
}
