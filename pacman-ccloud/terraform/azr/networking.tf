###########################################
################### VPC ###################
###########################################

resource "azurerm_virtual_network" "default" {
  name = var.global_prefix
  address_space = ["10.0.0.0/16"]
  location = local.region
  resource_group_name = azurerm_resource_group.azure_resource_group.name
  tags = {
    environment = var.global_prefix
  }
}

###########################################
################# Subnets #################
###########################################

resource "azurerm_subnet" "private_subnet" {
  name = "private-subnet-${var.global_prefix}"
  resource_group_name  = azurerm_resource_group.azure_resource_group.name
  virtual_network_name = azurerm_virtual_network.default.name
  address_prefix = "10.0.1.0/24"
}

resource "azurerm_subnet" "public_subnet" {
  name = "public-subnet-${var.global_prefix}"
  resource_group_name = azurerm_resource_group.azure_resource_group.name
  virtual_network_name = azurerm_virtual_network.default.name
  address_prefix = "10.0.2.0/24"
}
