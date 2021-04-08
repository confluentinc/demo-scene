provider "aws" {
  region = var.region
}

resource "aws_instance" "zookeeper" {
  count             = var.zookeeper_instance_count
  ami               = var.ami
  instance_type     = var.instance_type
  key_name          = var.ssh_key_pair
  vpc_security_group_ids = [aws_security_group.base.id, aws_security_group.zookeeper.id]
  subnet_id         = var.subnet
  user_data = <<-EOF
      #! /bin/bash
      echo "Hello world"
      EOF

  root_block_device {
      volume_type = "gp2"
      volume_size = 256
      delete_on_termination = true
  }

  tags = {
    unique_identifier = var.unique_identifier
    deployed_with = "cp-ansible-tools"
  }
}

resource "aws_instance" "kafka" {
  count             = var.kafka_instance_count
  ami               = var.ami
  instance_type     = var.instance_type
  key_name          = var.ssh_key_pair
  vpc_security_group_ids = [aws_security_group.base.id, aws_security_group.kafka.id, aws_security_group.mds.id]
  subnet_id         = var.subnet
  user_data = <<-EOF
      #! /bin/bash
      echo "Hello world"
      EOF

  root_block_device {
      volume_type = "gp2"
      volume_size = 512
      delete_on_termination = true
  }

  tags = {
    unique_identifier = var.unique_identifier
    deployed_with = "cp-ansible-tools"
  }
}

resource "aws_instance" "schema_registry" {
  count             = var.schema_registry_instance_count
  ami               = var.ami
  instance_type     = var.instance_type
  key_name          = var.ssh_key_pair
  vpc_security_group_ids = [aws_security_group.base.id, aws_security_group.schema_registry.id]
  subnet_id         = var.subnet
  user_data = <<-EOF
      #! /bin/bash
      echo "Hello world"
      EOF

  root_block_device {
      volume_type = "gp2"
      volume_size = 256
      delete_on_termination = true
  }

  tags = {
    unique_identifier = var.unique_identifier
    deployed_with = "cp-ansible-tools"
  }
}

resource "aws_instance" "connect" {
  count             = var.connect_instance_count
  ami               = var.ami
  instance_type     = var.instance_type
  key_name          = var.ssh_key_pair
  vpc_security_group_ids = [aws_security_group.base.id, aws_security_group.connect.id]
  subnet_id         = var.subnet
  user_data = <<-EOF
      #! /bin/bash
      echo "Hello world"
      EOF

  root_block_device {
      volume_type = "gp2"
      volume_size = 256
      delete_on_termination = true
  }

  tags = {
    unique_identifier = var.unique_identifier
    deployed_with = "cp-ansible-tools"
  }
}

resource "aws_instance" "rest_proxy" {
  count             = var.rest_proxy_instance_count
  ami               = var.ami
  instance_type     = var.instance_type
  key_name          = var.ssh_key_pair
  vpc_security_group_ids = [aws_security_group.base.id, aws_security_group.rest_proxy.id]
  subnet_id         = var.subnet
  user_data = <<-EOF
      #! /bin/bash
      echo "Hello world"
      EOF

  root_block_device {
      volume_type = "gp2"
      volume_size = 256
      delete_on_termination = true
  }

  tags = {
    unique_identifier = var.unique_identifier
    deployed_with = "cp-ansible-tools"
  }
}

resource "aws_instance" "ksql" {
  count             = var.ksql_instance_count
  ami               = var.ami
  instance_type     = var.instance_type
  key_name          = var.ssh_key_pair
  vpc_security_group_ids = [aws_security_group.base.id, aws_security_group.ksql.id]
  subnet_id         = var.subnet
  user_data = <<-EOF
      #! /bin/bash
      echo "Hello world"
      EOF

  root_block_device {
      volume_type = "gp2"
      volume_size = 256
      delete_on_termination = true
  }

  tags = {
    unique_identifier = var.unique_identifier
    deployed_with = "cp-ansible-tools"
  }
}

resource "aws_instance" "control_center" {
  count             = var.control_center_instance_count
  ami               = var.ami
  instance_type     = "t2.xlarge"
  key_name          = var.ssh_key_pair
  vpc_security_group_ids = [aws_security_group.base.id, aws_security_group.control_center.id]
  subnet_id         = var.subnet
  user_data = <<-EOF
      #! /bin/bash
      echo "Hello world"
      EOF

  root_block_device {
      volume_type = "gp2"
      volume_size = 512
      delete_on_termination = true
  }

  tags = {
    unique_identifier = var.unique_identifier
    deployed_with = "cp-ansible-tools"
  }
}
