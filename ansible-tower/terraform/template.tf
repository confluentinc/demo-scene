locals {
  hosts = <<EOT
all:
  vars:
    ansible_become: true
    ansible_ssh_common_args: -o StrictHostKeyChecking=no -o IdentitiesOnly=yes
    ansible_connection: ssh

    ssl_enabled: true

    kafka_broker_custom_listeners:
      external:
        name: EXTERNAL
        port: 9093

zookeeper:
  hosts:%{ for host in aws_instance.zookeeper.* }
    ${host.private_dns}:
      ansible_ssh_host: ${host.public_dns}%{ endfor }

kafka_broker:
  hosts:%{ for host in aws_instance.kafka.* }
    ${host.private_dns}:
      ansible_ssh_host: ${host.public_dns}
      mds_advertised_listener_hostname: ${host.public_dns}
      kafka_broker_custom_listeners:
        external:
          hostname: ${host.public_dns}%{ endfor }

schema_registry:
  hosts:%{ for host in aws_instance.schema_registry.* }
    ${host.private_dns}:
      ansible_ssh_host: ${host.public_dns}%{ endfor }

kafka_rest:
  hosts:%{ for host in aws_instance.rest_proxy.* }
    ${host.private_dns}:
      ansible_ssh_host: ${host.public_dns}%{ endfor }

kafka_connect:
  hosts:%{ for host in aws_instance.connect.* }
    ${host.private_dns}:
      ansible_ssh_host: ${host.public_dns}%{ endfor }

ksql:
  hosts:%{ for host in aws_instance.ksql.* }
    ${host.private_dns}:
      ansible_ssh_host: ${host.public_dns}%{ endfor }

control_center:
  hosts:%{ for host in aws_instance.control_center.* }
    ${host.private_dns}:
      ansible_ssh_host: ${host.public_dns}%{ endfor }
EOT
}

resource "null_resource" "hosts_provisioner" {
  provisioner "local-exec" {
    command = "echo \"${local.hosts}\" > hosts.yml"
  }
}
