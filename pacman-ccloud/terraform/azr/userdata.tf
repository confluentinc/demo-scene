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
}
