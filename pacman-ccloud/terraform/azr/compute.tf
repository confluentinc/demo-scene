###########################################
################ Key Pair #################
###########################################

resource "tls_private_key" "key_pair" {
  algorithm = "RSA"
  rsa_bits  = 4096
}

resource "local_file" "private_key" {
  content = tls_private_key.key_pair.private_key_pem
  filename = "cert.pem"
}

resource "null_resource" "private_key_permissions" {
  depends_on = [local_file.private_key]
  provisioner "local-exec" {
    command = "chmod 600 cert.pem"
    interpreter = ["bash", "-c"]
    on_failure = continue
  }
}

###########################################
################ Bootstrap ################
###########################################

resource "azurerm_storage_blob" "bastion_server_bootstrap" {
  count = var.instance_count["bastion_server"] >= 1 ? 1 : 0
  depends_on = [module.staticweb]
  name = "scripts/bootstrap/bastion-server.sh"
  content_type = "text/x-shellscript"
  storage_account_name = azurerm_storage_account.pacman.name
  storage_container_name = "$web"
  type = "Block"
  source_content = data.template_file.bastion_server_bootstrap.rendered
}

resource "azurerm_storage_blob" "rest_proxy_bootstrap" {
  count = var.instance_count["rest_proxy"] >= 1 ? 1 : 0
  depends_on = [module.staticweb]
  name = "scripts/bootstrap/rest-proxy.sh"
  content_type = "text/x-shellscript"
  storage_account_name = azurerm_storage_account.pacman.name
  storage_container_name = "$web"
  type = "Block"
  source_content = data.template_file.rest_proxy_bootstrap.rendered
}

resource "azurerm_storage_blob" "ksql_server_bootstrap" {
  count = var.instance_count["ksql_server"] >= 1 ? 1 : 0
  depends_on = [module.staticweb]
  name = "scripts/bootstrap/ksql-server.sh"
  content_type = "text/x-shellscript"
  storage_account_name = azurerm_storage_account.pacman.name
  storage_container_name = "$web"
  type = "Block"
  source_content = data.template_file.ksql_server_bootstrap.rendered
}

###########################################
############## REST Proxy #################
###########################################

resource "azurerm_availability_set" "rest_proxy" {
  count = var.instance_count["rest_proxy"] >= 1 ? 1 : 0
  name = "${var.global_prefix}-rest-proxy-avalset"
  location = local.region
  resource_group_name = azurerm_resource_group.azure_resource_group.name
  platform_fault_domain_count = 2
  platform_update_domain_count = 2
  managed = true
}

resource "azurerm_network_interface" "rest_proxy" {
  count = var.instance_count["rest_proxy"]
  name = "${var.global_prefix}-rest-proxy-${count.index}-nic"
  location = local.region
  resource_group_name = azurerm_resource_group.azure_resource_group.name
  ip_configuration {
    name = "${var.global_prefix}-rest-proxy-ip-config"
    subnet_id = azurerm_subnet.private_subnet.id
    private_ip_address_allocation = "dynamic"
    load_balancer_backend_address_pools_ids = [azurerm_lb_backend_address_pool.rest_proxy.id]
  }
}

resource "azurerm_virtual_machine" "rest_proxy" {
  count = var.instance_count["rest_proxy"]
  depends_on = [azurerm_storage_blob.rest_proxy_bootstrap]
  name = "${var.global_prefix}-rest-proxy-${count.index}"
  location = local.region
  availability_set_id = azurerm_availability_set.rest_proxy[0].id
  resource_group_name = azurerm_resource_group.azure_resource_group.name
  network_interface_ids = [azurerm_network_interface.rest_proxy[count.index].id]
  vm_size = "Standard_DS4_v2"
  storage_image_reference {
    publisher = "OpenLogic"
    offer = "CentOS"
    sku = "7.5"
    version = "latest"
  }
  storage_os_disk {
    name = "${var.global_prefix}-rest-proxy-${count.index}"
    caching = "ReadWrite"
    create_option = "FromImage"
    managed_disk_type = "Standard_LRS"
    disk_size_gb = 100
  }
  os_profile {
    computer_name  = "rest-proxy-${count.index}"
    admin_username = "azure"
    admin_password = "welcome1"
  }
  os_profile_linux_config {
    disable_password_authentication = true
    ssh_keys {
      path = "/home/azure/.ssh/authorized_keys"
      key_data = tls_private_key.key_pair.public_key_openssh
    }
  }
}

resource "azurerm_virtual_machine_extension" "rest_proxy" {
  count = var.instance_count["rest_proxy"]
  depends_on = [azurerm_virtual_machine.rest_proxy]
  name = "${var.global_prefix}-rest-proxy-${count.index}"
  location = local.region
  resource_group_name = azurerm_resource_group.azure_resource_group.name
  virtual_machine_name = azurerm_virtual_machine.rest_proxy[count.index].name
  publisher = "Microsoft.Azure.Extensions"
  type = "CustomScript"
  type_handler_version = "2.0"
  settings = <<SETTINGS
    {
      "fileUris": ["${azurerm_storage_account.pacman.primary_web_endpoint}${azurerm_storage_blob.rest_proxy_bootstrap[0].name}"],
      "commandToExecute": "./rest-proxy.sh exit 0"
    }
SETTINGS
}

###########################################
############## KSQL Server ################
###########################################

resource "azurerm_availability_set" "ksql_server" {
  count = var.instance_count["ksql_server"] >= 1 ? 1 : 0
  name = "${var.global_prefix}-ksql-server-avalset"
  location = local.region
  resource_group_name = azurerm_resource_group.azure_resource_group.name
  platform_fault_domain_count = 2
  platform_update_domain_count = 2
  managed = true
}

resource "azurerm_network_interface" "ksql_server" {
  count = var.instance_count["ksql_server"]
  name = "${var.global_prefix}-ksql-server-${count.index}-nic"
  location = local.region
  resource_group_name = azurerm_resource_group.azure_resource_group.name
  ip_configuration {
    name = "${var.global_prefix}-ksql-server-ip-config"
    subnet_id = azurerm_subnet.private_subnet.id
    private_ip_address_allocation = "dynamic"
    load_balancer_backend_address_pools_ids = [azurerm_lb_backend_address_pool.ksql_server.id]
  }
}

resource "azurerm_virtual_machine" "ksql_server" {
  count = var.instance_count["ksql_server"]
  depends_on = [azurerm_storage_blob.ksql_server_bootstrap]
  name = "${var.global_prefix}-ksql-server-${count.index}"
  location = local.region
  availability_set_id = azurerm_availability_set.ksql_server[0].id
  resource_group_name = azurerm_resource_group.azure_resource_group.name
  network_interface_ids = [azurerm_network_interface.ksql_server[count.index].id]
  vm_size = "Standard_DS4_v2"
  storage_image_reference {
    publisher = "OpenLogic"
    offer = "CentOS"
    sku = "7.5"
    version = "latest"
  }
  storage_os_disk {
    name = "${var.global_prefix}-ksql-server-${count.index}"
    caching = "ReadWrite"
    create_option = "FromImage"
    managed_disk_type = "Standard_LRS"
    disk_size_gb = 300
  }
  os_profile {
    computer_name  = "ksql-server-${count.index}"
    admin_username = "azure"
    admin_password = "welcome1"
  }
  os_profile_linux_config {
    disable_password_authentication = true
    ssh_keys {
      path = "/home/azure/.ssh/authorized_keys"
      key_data = tls_private_key.key_pair.public_key_openssh
    }
  }
}

resource "azurerm_virtual_machine_extension" "ksql_server" {
  count = var.instance_count["ksql_server"]
  depends_on = [azurerm_virtual_machine.ksql_server]
  name = "${var.global_prefix}-ksql-server-${count.index}"
  location = local.region
  resource_group_name = azurerm_resource_group.azure_resource_group.name
  virtual_machine_name = azurerm_virtual_machine.ksql_server[count.index].name
  publisher = "Microsoft.Azure.Extensions"
  type = "CustomScript"
  type_handler_version = "2.0"
  settings = <<SETTINGS
    {
      "fileUris": ["${azurerm_storage_account.pacman.primary_web_endpoint}${azurerm_storage_blob.ksql_server_bootstrap[0].name}"],
      "commandToExecute": "./ksql-server.sh exit 0"
    }
SETTINGS
}

###########################################
############# REST Proxy LBR ##############
###########################################

resource "azurerm_lb" "rest_proxy" {
  name = "${var.global_prefix}-rest-proxy"
  location = local.region
  resource_group_name = azurerm_resource_group.azure_resource_group.name
  frontend_ip_configuration {
    name = "${var.global_prefix}-rest-proxy"
    public_ip_address_id = azurerm_public_ip.rest_proxy[0].id
  }
}

resource "azurerm_lb_probe" "rest_proxy" {
  resource_group_name = azurerm_resource_group.azure_resource_group.name
  loadbalancer_id = azurerm_lb.rest_proxy.id
  name = "${var.global_prefix}-rest-proxy"
  protocol = "Http"
  request_path = "/"
  port = 8082
}

resource "azurerm_lb_rule" "rest_proxy" {
  resource_group_name = azurerm_resource_group.azure_resource_group.name
  loadbalancer_id = azurerm_lb.rest_proxy.id
  name = "${var.global_prefix}-rest-proxy"
  protocol = "Tcp"
  frontend_port = 80
  backend_port = 8082
  backend_address_pool_id = azurerm_lb_backend_address_pool.rest_proxy.id
  frontend_ip_configuration_name = "${var.global_prefix}-rest-proxy"
  probe_id = azurerm_lb_probe.rest_proxy.id
}

resource "azurerm_lb_backend_address_pool" "rest_proxy" {
  resource_group_name = azurerm_resource_group.azure_resource_group.name
  loadbalancer_id = azurerm_lb.rest_proxy.id
  name = "${var.global_prefix}-rest-proxy"
}

###########################################
############# KSQL Server LBR #############
###########################################

resource "azurerm_lb" "ksql_server" {
  name = "${var.global_prefix}-ksql-server"
  location = local.region
  resource_group_name = azurerm_resource_group.azure_resource_group.name
  frontend_ip_configuration {
    name = "${var.global_prefix}-ksql-server"
    public_ip_address_id = azurerm_public_ip.ksql_server[0].id
  }
}

resource "azurerm_lb_probe" "ksql_server" {
  resource_group_name = azurerm_resource_group.azure_resource_group.name
  loadbalancer_id = azurerm_lb.ksql_server.id
  name = "${var.global_prefix}-ksql-server"
  protocol = "Http"
  request_path = "/info"
  port = 8088
}

resource "azurerm_lb_rule" "ksql_server" {
  resource_group_name = azurerm_resource_group.azure_resource_group.name
  loadbalancer_id = azurerm_lb.ksql_server.id
  name = "${var.global_prefix}-ksql-server"
  protocol = "Tcp"
  frontend_port = 80
  backend_port = 8088
  backend_address_pool_id = azurerm_lb_backend_address_pool.ksql_server.id
  frontend_ip_configuration_name = "${var.global_prefix}-ksql-server"
  probe_id = azurerm_lb_probe.ksql_server.id
}

resource "azurerm_lb_backend_address_pool" "ksql_server" {
  resource_group_name = azurerm_resource_group.azure_resource_group.name
  loadbalancer_id = azurerm_lb.ksql_server.id
  name = "${var.global_prefix}-ksql-server"
}

###########################################
############ Bastion Server ###############
###########################################

resource "azurerm_network_interface" "bastion_server" {
  count = var.instance_count["bastion_server"] >= 1 ? 1 : 0
  name = "${var.global_prefix}-bastion-server-nic"
  location = local.region
  network_security_group_id = azurerm_network_security_group.bastion-server.id
  resource_group_name = azurerm_resource_group.azure_resource_group.name
  ip_configuration {
    name = "${var.global_prefix}-bastion-server-ip-config"
    subnet_id = azurerm_subnet.private_subnet.id
    private_ip_address_allocation = "dynamic"
    public_ip_address_id = azurerm_public_ip.bastion_server[0].id
  }
}

resource "azurerm_virtual_machine" "bastion_server" {
  depends_on = [azurerm_storage_blob.bastion_server_bootstrap]
  count = var.instance_count["bastion_server"] >= 1 ? 1 : 0
  name = "${var.global_prefix}-bastion-server"
  location = local.region
  resource_group_name = azurerm_resource_group.azure_resource_group.name
  network_interface_ids = [azurerm_network_interface.bastion_server[count.index].id]
  vm_size = "Standard_DS1_v2"
  storage_image_reference {
    publisher = "OpenLogic"
    offer = "CentOS"
    sku = "7.5"
    version = "latest"
  }
  storage_os_disk {
    name = "${var.global_prefix}-bastion-server-${count.index}"
    caching = "ReadWrite"
    create_option = "FromImage"
    managed_disk_type = "Standard_LRS"
  }
  os_profile {
    computer_name = "bastion-server-${count.index}"
    admin_username = "azure"
    admin_password = "welcome1"
  }
  os_profile_linux_config {
    disable_password_authentication = true
    ssh_keys {
      path = "/home/azure/.ssh/authorized_keys"
      key_data = tls_private_key.key_pair.public_key_openssh
    }
  }
}

resource "azurerm_virtual_machine_extension" "bastion_server" {
  count = var.instance_count["bastion_server"] >= 1 ? 1 : 0
  depends_on = [azurerm_virtual_machine.bastion_server]
  name = "${var.global_prefix}-bastion-server"
  location = local.region
  resource_group_name = azurerm_resource_group.azure_resource_group.name
  virtual_machine_name = azurerm_virtual_machine.bastion_server[count.index].name
  publisher = "Microsoft.Azure.Extensions"
  type = "CustomScript"
  type_handler_version = "2.0"
  settings = <<SETTINGS
    {
      "fileUris": ["${azurerm_storage_account.pacman.primary_web_endpoint}${azurerm_storage_blob.bastion_server_bootstrap[0].name}"],
      "commandToExecute": "./bastion-server.sh exit 0"
    }
SETTINGS
}
