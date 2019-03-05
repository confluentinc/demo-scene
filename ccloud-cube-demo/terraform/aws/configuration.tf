resource "null_resource" "local_config" {

    provisioner "local-exec" {

        command = "rm ~/.ccloud/config"
        interpreter = ["bash", "-c"]
        on_failure = "continue"

    }

    provisioner "local-exec" {

        command = "echo '${data.template_file.config_properties.rendered}' >> ~/.ccloud/config"
        interpreter = ["bash", "-c"]
        on_failure = "continue"

    }

    provisioner "local-exec" {

        command = "ccloud topic create _NUMBERS --partitions 4 --replication-factor 3"
        on_failure = "continue"

    }

    provisioner "local-exec" {

        command = "ccloud topic create _EVENTS --partitions 4 --replication-factor 3"
        on_failure = "continue"

    }

}

data "template_file" "initialize_script" {

  template = "${file("templates/initialize.sh")}"

  vars {

    schema_registry_url = "${join(",", formatlist("http://%s", aws_alb.schema_registry.*.dns_name))}"

    rest_proxy_url = "${join(",", formatlist("http://%s", aws_alb.rest_proxy.*.dns_name))}"

    ksql_server_url = "${join(",", formatlist("http://%s", aws_alb.ksql_server.*.dns_name))}"

  }

}

resource "local_file" "initialize" {

    content = "${data.template_file.initialize_script.rendered}"
    filename = "initialize.sh"

}