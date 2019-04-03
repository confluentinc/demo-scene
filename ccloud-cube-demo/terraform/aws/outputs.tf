###########################################
################# Outputs #################
###########################################

output "Schema Registry              " {

  value = "${join(",", formatlist("http://%s", aws_alb.schema_registry.*.dns_name))}"

}

output "REST Proxy                   " {

  value = "${var.instance_count["rest_proxy"] >= 1
           ? "${join(",", formatlist("http://%s", aws_alb.rest_proxy.*.dns_name))}"
           : "REST Proxy has been disabled"}"

}

output "KSQL Server                  " {

  value = "${var.instance_count["ksql_server"] >= 1
           ? "${join(",", formatlist("http://%s", aws_alb.ksql_server.*.dns_name))}"
           : "KSQL Server has been disabled"}"

}

output "Control Center               " {

  value = "${var.instance_count["control_center"] >= 1
           ? "${join(",", formatlist("http://%s", aws_alb.control_center.*.dns_name))}"
           : "Control Center has been disabled"}"

}

output "Bastion Server IP Address    " {

  value = "${var.instance_count["bastion_server"] >= 1
           ? "${join(",", formatlist("%s", aws_instance.bastion_server.*.public_ip))}"
           : "Bastion Server has been disabled"}"

}
output "Bastion Server Private Key   " {

  value = "${var.instance_count["bastion_server"] >= 1
           ? "${tls_private_key.key_pair.private_key_pem}"
           : "Bastion Server has been disabled"}"

}