###########################################
############ Other Local Files ############
###########################################

data "template_file" "ccloud_properties" {
  template = file("../../scoreboard/ccloud.template")
  vars = {
    bootstrap_server = var.bootstrap_server
    cluster_api_key = var.cluster_api_key
    cluster_api_secret = var.cluster_api_secret
    schema_registry_url = var.schema_registry_url
    schema_registry_username = split(":", var.schema_registry_basic_auth)[0]
    schema_registry_password = split(":", var.schema_registry_basic_auth)[1]
  }
}

resource "local_file" "ccloud_properties" {
  content  = data.template_file.ccloud_properties.rendered
  filename = "../../scoreboard/ccloud.properties"
}

data "template_file" "local_ksql_server" {
  template = file("../../pipeline/ksql-server.template")
  vars = {
    global_prefix = var.global_prefix
    bootstrap_server = var.bootstrap_server
    cluster_api_key = var.cluster_api_key
    cluster_api_secret = var.cluster_api_secret
    schema_registry_url = var.schema_registry_url
    schema_registry_basic_auth = var.schema_registry_basic_auth
  }
}

resource "local_file" "local_ksql_server" {
  content  = data.template_file.local_ksql_server.rendered
  filename = "../../pipeline/ksql-server.properties"
}
