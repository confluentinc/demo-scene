###########################################
################ Key Pair #################
###########################################

resource "tls_private_key" "key_pair" {
  algorithm = "RSA"
  rsa_bits = 4096
}

resource "aws_key_pair" "generated_key" {
  key_name = var.global_prefix
  public_key = tls_private_key.key_pair.public_key_openssh
}

resource "local_file" "private_key" {
  content = tls_private_key.key_pair.private_key_pem
  filename = "cert.pem"
}

resource "null_resource" "private_key_permissions" {
  depends_on = [local_file.private_key]
  provisioner "local-exec" {
    command = "chmod 600 cert.pem"
    interpreter = ["bash", "-c"]
    on_failure = continue
  }
}

###########################################
############## KSQL Server ################
###########################################

resource "aws_instance" "ksql_server" {
  depends_on = [
    aws_subnet.private_subnet,
    aws_nat_gateway.default,
  ]
  count = var.instance_count["ksql_server"]
  ami = data.aws_ami.amazon_linux_2.id
  instance_type = "t3.2xlarge"
  key_name = aws_key_pair.generated_key.key_name
  subnet_id = element(aws_subnet.private_subnet.*.id, count.index)
  vpc_security_group_ids = [aws_security_group.ksql_server[0].id]
  user_data = data.template_file.ksql_server_bootstrap.rendered
  root_block_device {
    volume_type = "gp2"
    volume_size = 300
  }
  tags = {
    Name = "${var.global_prefix}-ksql-server-${count.index}"
  }
}

###########################################
############ Bastion Server ###############
###########################################

resource "aws_instance" "bastion_server" {
  depends_on = [aws_instance.ksql_server]
  count = var.instance_count["bastion_server"] >= 1 ? 1 : 0
  ami = data.aws_ami.amazon_linux_2.id
  instance_type = "t2.micro"
  key_name = aws_key_pair.generated_key.key_name
  subnet_id = aws_subnet.bastion_server[0].id
  vpc_security_group_ids = [aws_security_group.bastion_server[0].id]
  user_data = data.template_file.bastion_server_bootstrap.rendered
  root_block_device {
    volume_type = "gp2"
    volume_size = 100
  }
  tags = {
    Name = "${var.global_prefix}-bastion-server"
  }
}

###########################################
############# KSQL Server LBR #############
###########################################

resource "aws_alb_target_group" "ksql_server_target_group" {
  count = var.instance_count["ksql_server"] >= 1 ? 1 : 0
  name = "${var.global_prefix}-ks-target-group"
  port = "8088"
  protocol = "HTTP"
  vpc_id = aws_vpc.default.id
  health_check {
    healthy_threshold = 3
    unhealthy_threshold = 3
    timeout = 3
    interval = 5
    path = "/info"
    port = "8088"
  }
}

resource "aws_alb_target_group_attachment" "ksql_server_attachment" {
  count = var.instance_count["ksql_server"] >= 1 ? var.instance_count["ksql_server"] : 0
  target_group_arn = aws_alb_target_group.ksql_server_target_group[0].arn
  target_id = element(aws_instance.ksql_server.*.id, count.index)
  port = 8088
}

resource "aws_alb" "ksql_server" {
  depends_on = [aws_instance.ksql_server]
  count = var.instance_count["ksql_server"] >= 1 ? 1 : 0
  name = "pacman${random_string.random_string.result}-ksql"
  subnets = [aws_subnet.public_subnet_1.id, aws_subnet.public_subnet_2.id]
  security_groups = [aws_security_group.load_balancer.id]
  internal = false
  tags = {
    Name = "pacman${random_string.random_string.result}-ksql"
  }
}

resource "aws_alb_listener" "ksql_server_listener" {
  count = var.instance_count["ksql_server"] >= 1 ? 1 : 0
  load_balancer_arn = aws_alb.ksql_server[0].arn
  protocol = "HTTP"
  port = "80"
  default_action {
    target_group_arn = aws_alb_target_group.ksql_server_target_group[0].arn
    type = "forward"
  }
}
