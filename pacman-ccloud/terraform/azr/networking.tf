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
  name = "${var.global_prefix}-private-subnet"
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
  domain_name_label = "pacman${random_string.random_string.result}-rest"
  resource_group_name = azurerm_resource_group.azure_resource_group.name
  allocation_method = "Static"
}

resource "azurerm_public_ip" "ksql_server" {
  name = "${var.global_prefix}-ksql-server"
  location = local.region
  domain_name_label = "pacman${random_string.random_string.result}-ksql"
  resource_group_name = azurerm_resource_group.azure_resource_group.name
  allocation_method = "Static"
}

resource "azurerm_public_ip" "bastion_server" {
  name = "${var.global_prefix}-bastion-server"
  location = local.region
  domain_name_label = "pacman${random_string.random_string.result}-ssh"
  resource_group_name = azurerm_resource_group.azure_resource_group.name
  allocation_method = "Static"
}

###########################################
############# Security Groups #############
###########################################

resource "azurerm_network_security_group" "private_subnet" {
  name = "${var.global_prefix}-private-subnet"
  location = local.region
  resource_group_name = azurerm_resource_group.azure_resource_group.name
  security_rule {
    name                       = "inbound-bastion-server"
    priority                   = 1001
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    source_port_range          = "*"
    destination_port_range     = "22"
    source_address_prefix      = "*"
    destination_address_prefix = "*"
  }
  security_rule {
    name                       = "inbound-rest-proxy"
    priority                   = 1002
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    source_port_range          = "*"
    destination_port_range     = "8082"
    source_address_prefix      = "*"
    destination_address_prefix = "*"
  }
  security_rule {
    name                       = "inbound-ksql-server"
    priority                   = 1003
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    source_port_range          = "*"
    destination_port_range     = "8088"
    source_address_prefix      = "*"
    destination_address_prefix = "*"
  }
}

resource "azurerm_subnet_network_security_group_association" "private_subnet" {
  subnet_id = azurerm_subnet.private_subnet.id
  network_security_group_id = azurerm_network_security_group.private_subnet.id
}