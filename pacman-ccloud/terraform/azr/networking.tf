###########################################
############# Virtual Network #############
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

###########################################
############### Public IPs ################
###########################################

resource "azurerm_public_ip" "rest_proxy" {
  name = "${var.global_prefix}-rest-proxy"
  location = local.region
  domain_name_label = "pacmanccloud${random_string.random_string.result}"
  resource_group_name = azurerm_resource_group.azure_resource_group.name
  allocation_method = "Static"
}

resource "azurerm_public_ip" "bastion_server" {
  name = "${var.global_prefix}-bastion-server"
  location = local.region
  domain_name_label = "pacmanccloud${random_string.random_string.result}-ssh"
  resource_group_name = azurerm_resource_group.azure_resource_group.name
  allocation_method = "Static"
}
