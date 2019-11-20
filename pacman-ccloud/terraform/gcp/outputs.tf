###########################################
################# Outputs #################
###########################################

output "Pacman" {
  value = format("http://%s", google_compute_global_address.pacman.address)
}

output "KSQL_Server" {
  value = var.instance_count["ksql_server"] >= 1 ? join(",", formatlist("http://%s", google_compute_global_address.ksql_server.*.address,),) : "KSQL Server has been disabled"
}

/************** Uncomment this if you need access to the REST Proxy endpoint **************
output "REST_Proxy" {
  value = var.instance_count["rest_proxy"] >= 1 ? join(",", formatlist("http://%s", google_compute_global_address.rest_proxy.*.address,),) : "REST Proxy has been disabled"
}
*/
