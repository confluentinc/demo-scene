###########################################
################# Outputs #################
###########################################

output "Pacman" {
  value = replace(azurerm_storage_account.pacman.primary_web_endpoint, "https", "http")
}

output "REST_Proxy" {
  value = var.instance_count["rest_proxy"] >= 1 ? "http://${azurerm_public_ip.rest_proxy[0].fqdn}" : "REST Proxy has been disabled"
}

output "KSQL_Server" {
  value = var.instance_count["ksql_server"] >= 1 ? "http://${azurerm_public_ip.ksql_server[0].fqdn}" : "KSQL Server has been disabled"
}

output "Bastion_Server" {
  value = var.instance_count["bastion_server"] >= 1 ? "ssh azure@${azurerm_public_ip.bastion_server[0].fqdn} -i cert.pem" : "Bastion Server has been disabled"
}
