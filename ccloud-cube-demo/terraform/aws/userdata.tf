###########################################
######## Schema Registry Bootstrap ########
###########################################

data "template_file" "schema_registry_properties" {

  template = "${file("../util/schema-registry.properties")}"

  vars {

    broker_list = "${var.ccloud_broker_list}"
    access_key = "${var.ccloud_access_key}"
    secret_key = "${var.ccloud_secret_key}"

  }

}

data "template_file" "schema_registry_bootstrap" {

  template = "${file("../util/schema-registry.sh")}"

  vars {

    confluent_platform_location = "${var.confluent_platform_location}"
    schema_registry_properties = "${data.template_file.schema_registry_properties.rendered}"

  }

}

###########################################
######### REST Proxy Bootstrap ############
###########################################

data "template_file" "rest_proxy_properties" {

  template = "${file("../util/rest-proxy.properties")}"

  vars {

    broker_list = "${var.ccloud_broker_list}"
    access_key = "${var.ccloud_access_key}"
    secret_key = "${var.ccloud_secret_key}"

    schema_registry_url = "${join(",", formatlist("http://%s:%s",
      aws_instance.schema_registry.*.private_ip, "8081"))}"

  }

}

data "template_file" "rest_proxy_bootstrap" {

  template = "${file("../util/rest-proxy.sh")}"

  vars {

    jaeger_tracing_location = "${var.jaeger_tracing_location}"
    jaeger_collector = "${join(",", formatlist("%s:%s",
      aws_instance.jaeger_server.*.private_ip, "14267"))}"

    confluent_platform_location = "${var.confluent_platform_location}"
    rest_proxy_properties = "${data.template_file.rest_proxy_properties.rendered}"

    schema_registry_url = "${join(",", formatlist("http://%s:%s",
      aws_instance.schema_registry.*.private_ip, "8081"))}"

  }

}

###########################################
######## Kafka Connect Bootstrap ##########
###########################################

data "template_file" "kafka_connect_properties" {

  template = "${file("../util/kafka-connect.properties")}"

  vars {

    broker_list = "${var.ccloud_broker_list}"
    access_key = "${var.ccloud_access_key}"
    secret_key = "${var.ccloud_secret_key}"

    schema_registry_url = "${join(",", formatlist("http://%s:%s",
      aws_instance.schema_registry.*.private_ip, "8081"))}"

  }

}

data "template_file" "kafka_connect_bootstrap" {

  template = "${file("../util/kafka-connect.sh")}"

  vars {

    confluent_platform_location = "${var.confluent_platform_location}"
    kafka_connect_properties = "${data.template_file.kafka_connect_properties.rendered}"

  }

}

###########################################
######### KSQL Server Bootstrap ###########
###########################################

data "template_file" "ksql_server_properties" {

  template = "${file("../util/ksql-server.properties")}"

  vars {

    broker_list = "${var.ccloud_broker_list}"
    access_key = "${var.ccloud_access_key}"
    secret_key = "${var.ccloud_secret_key}"

    schema_registry_url = "${join(",", formatlist("http://%s:%s",
      aws_instance.schema_registry.*.private_ip, "8081"))}"

  }

}

data "template_file" "ksql_server_bootstrap" {

  template = "${file("../util/ksql-server.sh")}"

  vars {

    jaeger_tracing_location = "${var.jaeger_tracing_location}"
    jaeger_collector = "${join(",", formatlist("%s:%s",
      aws_instance.jaeger_server.*.private_ip, "14267"))}"

    confluent_platform_location = "${var.confluent_platform_location}"
    ksql_server_properties = "${data.template_file.ksql_server_properties.rendered}"
    broker_list = "${var.ccloud_broker_list}"

  }

}

###########################################
######## Control Center Bootstrap #########
###########################################

data "template_file" "control_center_properties" {

  template = "${file("../util/control-center.properties")}"

  vars {

    broker_list = "${var.ccloud_broker_list}"
    access_key = "${var.ccloud_access_key}"
    secret_key = "${var.ccloud_secret_key}"

    schema_registry_url = "${join(",", formatlist("http://%s:%s",
      aws_instance.schema_registry.*.private_ip, "8081"))}"

    kafka_connect_url = "${join(",", formatlist("http://%s:%s",
      aws_instance.kafka_connect.*.private_ip, "8083"))}"

    ksql_server_url = "${join(",", formatlist("http://%s:%s",
      aws_instance.ksql_server.*.private_ip, "8088"))}"

    ksql_public_url = "${join(",", formatlist("http://%s:%s",
      aws_alb.ksql_server.*.dns_name, "80"))}"

  }

}

data "template_file" "control_center_bootstrap" {

  template = "${file("../util/control-center.sh")}"

  vars {

    confluent_platform_location = "${var.confluent_platform_location}"
    control_center_properties = "${data.template_file.control_center_properties.rendered}"

  }

}

###########################################
######### Jaeger Server Bootstrap #########
###########################################

data "template_file" "jaeger_server_bootstrap" {

  template = "${file("../util/jaeger-server.sh")}"

  vars {

    jaeger_tracing_location = "${var.jaeger_tracing_location}"

  }

}

###########################################
######## Bastion Server Bootstrap #########
###########################################

data "template_file" "bastion_server_bootstrap" {

  template = <<EOT

    #!/bin/bash
    yum update -y

  EOT
  
}