resource "aws_security_group" "base" {
  name        = "base_${var.unique_identifier}"
  vpc_id      = var.vpc_id
  description = "CP-Ansible Testing - Base SG"

  # ICMP (any)
  ingress {
    from_port   = "-1"
    to_port     = "-1"
    protocol    = "icmp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = "5000"
    to_port     = "5000"
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # SSH
  ingress {
    from_port   = "22"
    to_port     = "22"
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port       = 0
    to_port         = 0
    protocol        = "-1"
    cidr_blocks     = ["0.0.0.0/0"]
  }

  tags = {
    unique_identifier = var.unique_identifier
    deployed_with = "cp-ansible-tools"
  }
}

resource "aws_security_group" "zookeeper" {
  name        = "zookeeper_${var.unique_identifier}"
  vpc_id      = var.vpc_id
  description = "CP-Ansible Testing - Zookeeper SG"

  # 2181 for Non TLS
  ingress {
    from_port       = "2181"
    to_port         = "2181"
    protocol        = "tcp"
    self            = true
    security_groups = [aws_security_group.kafka.id, aws_security_group.schema_registry.id, aws_security_group.connect.id, aws_security_group.rest_proxy.id]
  }

  # 2182 for Non TLS
  ingress {
    from_port       = "2182"
    to_port         = "2182"
    protocol        = "tcp"
    self            = true
    security_groups = [aws_security_group.kafka.id, aws_security_group.schema_registry.id, aws_security_group.connect.id, aws_security_group.rest_proxy.id]
  }

  ingress {
    from_port   = "2888"
    to_port     = "2888"
    protocol    = "tcp"
    self        = true
  }

  ingress {
    from_port   = "3888"
    to_port     = "3888"
    protocol    = "tcp"
    self        = true
  }

  tags = {
    unique_identifier = var.unique_identifier
    deployed_with = "cp-ansible-tools"
  }
}

resource "aws_security_group" "kafka" {
  name        = "kafka_${var.unique_identifier}"
  vpc_id      = var.vpc_id
  description = "CP-Ansible Testing - Kafka SG"

  ingress {
    from_port   = "9091"
    to_port     = "9091"
    protocol    = "tcp"
    self        = true
    security_groups = [aws_security_group.schema_registry.id, aws_security_group.rest_proxy.id, aws_security_group.connect.id, aws_security_group.ksql.id, aws_security_group.control_center.id]
  }

  # TODO figure out this one, seems like we are doing one listener for kafka, one for components and one for external
  ingress {
    from_port   = "9092"
    to_port     = "9092"
    protocol    = "tcp"
    self        = true
    security_groups = [aws_security_group.schema_registry.id, aws_security_group.rest_proxy.id, aws_security_group.connect.id, aws_security_group.ksql.id, aws_security_group.control_center.id]
  }

  # External port
  ingress {
    from_port   = "9093"
    to_port     = "9093"
    protocol    = "tcp"
    self        = true
    security_groups = [aws_security_group.schema_registry.id, aws_security_group.rest_proxy.id, aws_security_group.connect.id, aws_security_group.ksql.id, aws_security_group.control_center.id]
    cidr_blocks = ["0.0.0.0/0"]
  }

  # External port
  ingress {
    from_port   = "9094"
    to_port     = "9094"
    protocol    = "tcp"
    self        = true
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    unique_identifier = var.unique_identifier
    deployed_with = "cp-ansible-tools"
  }
}

resource "aws_security_group" "mds" {
  name        = "mds_${var.unique_identifier}"
  vpc_id      = var.vpc_id
  description = "CP-Ansible Testing - MDS SG"

  ingress {
    from_port   = "8090"
    to_port     = "8090"
    protocol    = "tcp"
    self        = true
    security_groups = [aws_security_group.schema_registry.id, aws_security_group.rest_proxy.id, aws_security_group.connect.id, aws_security_group.ksql.id, aws_security_group.control_center.id]
  }

  tags = {
    unique_identifier = var.unique_identifier
    deployed_with = "cp-ansible-tools"
  }
}

resource "aws_security_group" "rest_proxy" {
  name        = "rest_proxy_${var.unique_identifier}"
  vpc_id      = var.vpc_id
  description = "CP-Ansible Testing - Rest Proxy SG"

  ingress {
    from_port   = "8082"
    to_port     = "8082"
    protocol    = "tcp"
    self        = true
    # cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    unique_identifier = var.unique_identifier
    deployed_with = "cp-ansible-tools"
  }
}

resource "aws_security_group" "connect" {
  name        = "connect_${var.unique_identifier}"
  vpc_id      = var.vpc_id
  description = "CP-Ansible Testing - Connect SG"

  ingress {
    from_port   = "8083"
    to_port     = "8083"
    protocol    = "tcp"
    self        = true
    security_groups = [aws_security_group.control_center.id]
    # cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    unique_identifier = var.unique_identifier
    deployed_with = "cp-ansible-tools"
  }
}

resource "aws_security_group" "ksql" {
  name        = "ksql_${var.unique_identifier}"
  vpc_id      = var.vpc_id
  description = "CP-Ansible Testing - KSQL SG"

  ingress {
    from_port   = "8088"
    to_port     = "8088"
    protocol    = "tcp"
    self        = true
    security_groups = [aws_security_group.control_center.id]
    # cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    unique_identifier = var.unique_identifier
    deployed_with = "cp-ansible-tools"
  }
}

resource "aws_security_group" "control_center" {
  name        = "control_center_${var.unique_identifier}"
  vpc_id      = var.vpc_id
  description = "CP-Ansible Testing - C3 SG"

  ingress {
    from_port   = "9021"
    to_port     = "9021"
    protocol    = "tcp"
    self        = true
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    unique_identifier = var.unique_identifier
    deployed_with = "cp-ansible-tools"
  }
}

resource "aws_security_group" "kerberos" {
  name        = "kerberos_${var.unique_identifier}"
  vpc_id      = var.vpc_id
  description = "CP-Ansible Testing - Kerberos SG"

  ingress {
    from_port   = "88"
    to_port     = "88"
    protocol    = "tcp"
    self        = true
    security_groups = [aws_security_group.zookeeper.id, aws_security_group.kafka.id, aws_security_group.schema_registry.id, aws_security_group.rest_proxy.id, aws_security_group.connect.id, aws_security_group.ksql.id, aws_security_group.control_center.id]
  }

  ingress {
    from_port   = "749"
    to_port     = "749"
    protocol    = "tcp"
    self        = true
    security_groups = [aws_security_group.zookeeper.id, aws_security_group.kafka.id, aws_security_group.schema_registry.id, aws_security_group.rest_proxy.id, aws_security_group.connect.id, aws_security_group.ksql.id, aws_security_group.control_center.id]
  }

  tags = {
    unique_identifier = var.unique_identifier
    deployed_with = "cp-ansible-tools"
  }
}

resource "aws_security_group" "ldap" {
  name        = "ldap_${var.unique_identifier}"
  vpc_id      = var.vpc_id
  description = "CP-Ansible Testing - LDAP SG"

  ingress {
    from_port   = "389"
    to_port     = "389"
    protocol    = "tcp"
    self        = true
    security_groups = [aws_security_group.zookeeper.id, aws_security_group.kafka.id, aws_security_group.schema_registry.id, aws_security_group.rest_proxy.id, aws_security_group.connect.id, aws_security_group.ksql.id, aws_security_group.control_center.id]
  }

  tags = {
    unique_identifier = var.unique_identifier
    deployed_with = "cp-ansible-tools"
  }
}

# Theres a "Cycle" with kafka and schema_registry security groups, ie both reference each other, so have to do this
resource "aws_security_group" "schema_registry" {
  name        = "schema_registry_${var.unique_identifier}"
  description = "CP-Ansible Testing - Schema Registry SG"

  tags = {
    unique_identifier = var.unique_identifier
    deployed_with = "cp-ansible-tools"
  }
}

resource "aws_security_group_rule" "schema_registry-self" {
    security_group_id = aws_security_group.schema_registry.id
    type              = "ingress"
    from_port         = 8081
    to_port           = 8081
    protocol          = "tcp"
    self              = true
}

resource "aws_security_group_rule" "schema_registry-kafka" {
    security_group_id         = aws_security_group.schema_registry.id
    source_security_group_id  = aws_security_group.kafka.id
    type        = "ingress"
    from_port   = 8081
    to_port     = 8081
    protocol    = "tcp"
}

resource "aws_security_group_rule" "schema_registry-ksql" {
    security_group_id         = aws_security_group.schema_registry.id
    source_security_group_id  = aws_security_group.ksql.id
    type        = "ingress"
    from_port   = 8081
    to_port     = 8081
    protocol    = "tcp"
}

resource "aws_security_group_rule" "schema_registry-rest_proxy" {
    security_group_id         = aws_security_group.schema_registry.id
    source_security_group_id  = aws_security_group.rest_proxy.id
    type        = "ingress"
    from_port   = 8081
    to_port     = 8081
    protocol    = "tcp"
}

resource "aws_security_group_rule" "schema_registry-control_center" {
    security_group_id         = aws_security_group.schema_registry.id
    source_security_group_id  = aws_security_group.control_center.id
    type        = "ingress"
    from_port   = 8081
    to_port     = 8081
    protocol    = "tcp"
}
