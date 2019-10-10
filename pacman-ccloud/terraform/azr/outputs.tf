###########################################
################# Outputs #################
###########################################

output "Pacman" {
  value = azurerm_storage_account.pacman.primary_web_endpoint
}

output "REST_Proxy" {
  value = "http://${azurerm_public_ip.rest_proxy.fqdn}"
}

/*output "KSQL_Server" {
  value = var.instance_count["ksql_server"] >= 1 ? join(",", formatlist("http://%s", aws_alb.ksql_server.*.dns_name)) : "KSQL Server has been disabled"
}*/

output "Bastion_Server" {
  value = var.instance_count["bastion_server"] >= 1 ? "ssh azure@${join(",", formatlist("%s", azurerm_public_ip.bastion_server.*.fqdn),)}" : "Bastion Server has been disabled"
}
