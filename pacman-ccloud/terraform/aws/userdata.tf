###########################################
######### REST Proxy Bootstrap ############
###########################################

data "template_file" "rest_proxy_properties" {
  template = file("../util/rest-proxy.properties")
  vars = {
    bootstrap_server           = var.bootstrap_server
    cluster_api_key            = var.cluster_api_key
    cluster_api_secret         = var.cluster_api_secret
    global_prefix              = var.global_prefix
    confluent_home_value       = var.confluent_home_value
    schema_registry_url        = var.schema_registry_url
    schema_registry_basic_auth = var.schema_registry_basic_auth
  }
}

data "template_file" "rest_proxy_bootstrap" {
  template = file("../util/rest-proxy.sh")
  vars = {
    confluent_platform_location = var.confluent_platform_location
    bootstrap_server            = var.bootstrap_server
    cluster_api_key             = var.cluster_api_key
    cluster_api_secret          = var.cluster_api_secret
    rest_proxy_properties       = data.template_file.rest_proxy_properties.rendered
    confluent_home_value        = var.confluent_home_value
    schema_registry_url         = var.schema_registry_url
    schema_registry_basic_auth  = var.schema_registry_basic_auth
  }
}

###########################################
######### KSQL Server Bootstrap ###########
###########################################

data "template_file" "ksql_server_properties" {
  template = file("../util/ksql-server.properties")
  vars = {
    bootstrap_server           = var.bootstrap_server
    cluster_api_key            = var.cluster_api_key
    cluster_api_secret         = var.cluster_api_secret
    global_prefix              = var.global_prefix
    confluent_home_value       = var.confluent_home_value
    schema_registry_url        = var.schema_registry_url
    schema_registry_basic_auth = var.schema_registry_basic_auth
  }
}

data "template_file" "ksql_server_bootstrap" {
  template = file("../util/ksql-server.sh")
  vars = {
    confluent_platform_location = var.confluent_platform_location
    ksql_server_properties      = data.template_file.ksql_server_properties.rendered
    confluent_home_value        = var.confluent_home_value
  }
}

###########################################
######## Bastion Server Bootstrap #########
###########################################

data "template_file" "bastion_server_bootstrap" {
  template = file("../util/bastion-server.sh")
  vars = {
    user = "ec2-user"
    private_key_pem = tls_private_key.key_pair.private_key_pem
    rest_proxy_addresses = join(" ", formatlist("%s", aws_instance.rest_proxy.*.private_ip))
    ksql_server_addresses = join(" ", formatlist("%s", aws_instance.ksql_server.*.private_ip))
  }
}

###########################################
############ Other Local Files ############
###########################################

data "template_file" "ccloud_properties" {
  template = file("../../scoreboard/ccloud.template")
  vars = {
    bootstrap_server          = var.bootstrap_server
    cluster_api_key           = var.cluster_api_key
    cluster_api_secret        = var.cluster_api_secret
    schema_registry_url       = var.schema_registry_url
    schema_registry_username  = split(":", var.schema_registry_basic_auth)[0]
    schema_registry_password  = split(":", var.schema_registry_basic_auth)[1]
  }
}

resource "local_file" "ccloud_properties" {
  content  = data.template_file.ccloud_properties.rendered
  filename = "../../scoreboard/ccloud.properties"
}

data "template_file" "local_ksql_server" {
  template = file("../../pipeline/ksql-server.template")
  vars = {
    global_prefix               = var.global_prefix
    bootstrap_server            = var.bootstrap_server
    cluster_api_key             = var.cluster_api_key
    cluster_api_secret          = var.cluster_api_secret
    schema_registry_url         = var.schema_registry_url
    schema_registry_basic_auth  = var.schema_registry_basic_auth
  }
}

resource "local_file" "local_ksql_server" {
  content  = data.template_file.local_ksql_server.rendered
  filename = "../../pipeline/ksql-server.properties"
}
