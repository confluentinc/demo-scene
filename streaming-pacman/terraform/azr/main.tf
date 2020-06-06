###########################################
################# Azure ###################
###########################################

provider "azurerm" {
  subscription_id = var.azure_subscription_id
  client_id = var.azure_client_id
  client_secret = var.azure_client_secret
  tenant_id = var.azure_tenant_id
}

resource "azurerm_resource_group" "azure_resource_group" {
  name = var.global_prefix
  location = local.region
  tags = {
    environment = var.global_prefix
  }
}

resource "random_string" "random_string" {
  length  = 8
  special = false
  upper = false
  lower = true
  number = true
}

data "template_file" "storage_account_pacman" {
  template = "pacman${random_string.random_string.result}"
}

resource "azurerm_storage_account" "pacman" {
  name = data.template_file.storage_account_pacman.rendered
  resource_group_name = azurerm_resource_group.azure_resource_group.name
  location = local.region
  account_tier = "Standard"
  account_replication_type = "GRS"
  account_kind = "StorageV2"
}

module "staticweb" {
  source = "StefanSchoof/static-website/azurerm"
  storage_account_name = azurerm_storage_account.pacman.name
  notfound_document = "error.html"
}

variable "azure_subscription_id" {
}

variable "azure_client_id" {
}

variable "azure_client_secret" {
}

variable "azure_tenant_id" {
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
