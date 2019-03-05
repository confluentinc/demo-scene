###########################################
################ Key Pair #################
###########################################

resource "tls_private_key" "key_pair" {

  algorithm = "RSA"
  rsa_bits  = 4096

}
resource "aws_key_pair" "generated_key" {

  key_name   = "${var.global_prefix}"
  public_key = "${tls_private_key.key_pair.public_key_openssh}"

}

###########################################
############ Schema Registry ##############
###########################################

resource "aws_instance" "schema_registry" {

  depends_on = ["aws_subnet.private_subnet",
                "aws_nat_gateway.default"]

  count = "${var.instance_count["schema_registry"] >= 1
           ? var.instance_count["schema_registry"] : 1}"

  ami = "ami-0922553b7b0369273"
  instance_type = "t3.medium"
  key_name = "${aws_key_pair.generated_key.key_name}"

  subnet_id = "${element(aws_subnet.private_subnet.*.id, count.index)}"
  vpc_security_group_ids = ["${aws_security_group.schema_registry.id}"]

  user_data = "${data.template_file.schema_registry_bootstrap.rendered}"

  ebs_block_device {

    device_name = "/dev/xvdb"
    volume_type = "gp2"
    volume_size = 100

  }

  tags {

    Name = "${var.global_prefix}-schema-registry-${count.index}"

  }

}

###########################################
############## REST Proxy #################
###########################################

resource "aws_instance" "rest_proxy" {

  depends_on = ["aws_instance.schema_registry"]

  count = "${var.instance_count["rest_proxy"]}"
  ami = "ami-0922553b7b0369273"
  instance_type = "t3.medium"
  key_name = "${aws_key_pair.generated_key.key_name}"

  subnet_id = "${element(aws_subnet.private_subnet.*.id, count.index)}"
  vpc_security_group_ids = ["${aws_security_group.rest_proxy.id}"]
  
  user_data = "${data.template_file.rest_proxy_bootstrap.rendered}"

  ebs_block_device {

    device_name = "/dev/xvdb"
    volume_type = "gp2"
    volume_size = 100

  }

  tags {

    Name = "${var.global_prefix}-rest-proxy-${count.index}"

  }

}

###########################################
############# Kafka Connect ###############
###########################################

resource "aws_instance" "kafka_connect" {

  depends_on = ["aws_instance.schema_registry"]

  count = "${var.instance_count["kafka_connect"]}"
  ami = "ami-0922553b7b0369273"
  instance_type = "t3.medium"
  key_name = "${aws_key_pair.generated_key.key_name}"

  subnet_id = "${element(aws_subnet.private_subnet.*.id, count.index)}"
  vpc_security_group_ids = ["${aws_security_group.kafka_connect.id}"]
  
  user_data = "${data.template_file.kafka_connect_bootstrap.rendered}"

  ebs_block_device {

    device_name = "/dev/xvdb"
    volume_type = "gp2"
    volume_size = 100

  }

  tags {

    Name = "${var.global_prefix}-kafka-connect-${count.index}"

  }

}

###########################################
############## KSQL Server ################
###########################################

resource "aws_instance" "ksql_server" {

  depends_on = ["aws_instance.rest_proxy"]

  count = "${var.instance_count["ksql_server"]}"
  ami = "ami-0922553b7b0369273"
  instance_type = "t3.2xlarge"
  key_name = "${aws_key_pair.generated_key.key_name}"

  subnet_id = "${element(aws_subnet.private_subnet.*.id, count.index)}"
  vpc_security_group_ids = ["${aws_security_group.ksql_server.id}"]
  
  user_data = "${data.template_file.ksql_server_bootstrap.rendered}"

  ebs_block_device {

    device_name = "/dev/xvdb"
    volume_type = "gp2"
    volume_size = 300

  }

  tags {

    Name = "${var.global_prefix}-ksql-server-${count.index}"

  }

}

###########################################
############ Control Center ###############
###########################################

resource "aws_instance" "control_center" {

  depends_on = ["aws_instance.ksql_server"]

  count = "${var.instance_count["control_center"]}"
  ami = "ami-0922553b7b0369273"
  instance_type = "t3.2xlarge"
  key_name = "${aws_key_pair.generated_key.key_name}"

  subnet_id = "${element(aws_subnet.private_subnet.*.id, count.index)}"
  vpc_security_group_ids = ["${aws_security_group.control_center.id}"]
  
  user_data = "${data.template_file.control_center_bootstrap.rendered}"

  ebs_block_device {

    device_name = "/dev/xvdb"
    volume_type = "gp2"
    volume_size = 300

  }

  tags {

    Name = "${var.global_prefix}-control-center-${count.index}"

  }

}

###########################################
############# Jaeger Server ###############
###########################################

resource "aws_instance" "jaeger_server" {

  depends_on = ["aws_subnet.private_subnet",
                "aws_nat_gateway.default"]

  count = "${var.instance_count["jaeger_server"] >= 1
           ? var.instance_count["jaeger_server"] : 1}"

  ami = "ami-0922553b7b0369273"
  instance_type = "t3.medium"
  key_name = "${aws_key_pair.generated_key.key_name}"

  subnet_id = "${element(aws_subnet.private_subnet.*.id, count.index)}"
  vpc_security_group_ids = ["${aws_security_group.jaeger_server.id}"]

  user_data = "${data.template_file.jaeger_server_bootstrap.rendered}"

  ebs_block_device {

    device_name = "/dev/xvdb"
    volume_type = "gp2"
    volume_size = 100

  }

  tags {

    Name = "${var.global_prefix}-jaeger-server-${count.index}"

  }

}

###########################################
############ Bastion Server ###############
###########################################

resource "aws_instance" "bastion_server" {

  count = "${var.instance_count["bastion_server"] >= 1 ? 1 : 0}"

  ami = "ami-0922553b7b0369273"
  instance_type = "t2.micro"
  key_name = "${aws_key_pair.generated_key.key_name}"

  subnet_id = "${aws_subnet.bastion_server.id}"
  vpc_security_group_ids = ["${aws_security_group.bastion_server.id}"]

  user_data = "${data.template_file.bastion_server_bootstrap.rendered}"

  ebs_block_device {

    device_name = "/dev/xvdb"
    volume_type = "gp2"
    volume_size = 10

  }

  tags {

    Name = "${var.global_prefix}-bastion-server"

  }

}

###########################################
########## Schema Registry LBR ############
###########################################

resource "aws_alb_target_group" "schema_registry_target_group" {

  name = "${var.global_prefix}-sr-target-group"  
  port = "8081"
  protocol = "HTTP"
  vpc_id = "${aws_vpc.default.id}"

  health_check {    

    healthy_threshold = 3    
    unhealthy_threshold = 3    
    timeout = 3   
    interval = 5    
    path = "/"
    port = "8081"

  }

}

resource "aws_alb_target_group_attachment" "schema_registry_attachment" {

  count = "${var.instance_count["schema_registry"] >= 1
           ? var.instance_count["schema_registry"] : 1}"

  target_group_arn = "${aws_alb_target_group.schema_registry_target_group.arn}"
  target_id = "${element(aws_instance.schema_registry.*.id, count.index)}"
  port = 8081

}

resource "aws_alb" "schema_registry" {

  depends_on = ["aws_instance.schema_registry"]

  name = "${var.global_prefix}-schema-registry"
  subnets = ["${aws_subnet.public_subnet_1.id}", "${aws_subnet.public_subnet_2.id}"]
  security_groups = ["${aws_security_group.load_balancer.id}"]
  internal = false

  tags {

    Name = "${var.global_prefix}-schema-registry"

  }

}

resource "aws_alb_listener" "schema_registry_listener" {

  load_balancer_arn = "${aws_alb.schema_registry.arn}"
  protocol = "HTTP"
  port = "80"
  
  default_action {

    target_group_arn = "${aws_alb_target_group.schema_registry_target_group.arn}"
    type = "forward"

  }

}

###########################################
############# REST Proxy LBR ##############
###########################################

resource "aws_alb_target_group" "rest_proxy_target_group" {

  count = "${var.instance_count["rest_proxy"] >= 1 ? 1 : 0}"

  name = "${var.global_prefix}-rp-target-group"  
  port = "8082"
  protocol = "HTTP"
  vpc_id = "${aws_vpc.default.id}"

  health_check {    

    healthy_threshold = 3    
    unhealthy_threshold = 3    
    timeout = 3   
    interval = 5    
    path = "/"
    port = "8082"

  }

}

resource "aws_alb_target_group_attachment" "rest_proxy_attachment" {

  count = "${var.instance_count["rest_proxy"] >= 1
           ? var.instance_count["rest_proxy"] : 0}"

  target_group_arn = "${aws_alb_target_group.rest_proxy_target_group.arn}"
  target_id = "${element(aws_instance.rest_proxy.*.id, count.index)}"
  port = 8082

}

resource "aws_alb" "rest_proxy" {

  depends_on = ["aws_instance.rest_proxy"]
  count = "${var.instance_count["rest_proxy"] >= 1 ? 1 : 0}"

  name = "${var.global_prefix}-rest-proxy"
  subnets = ["${aws_subnet.public_subnet_1.id}", "${aws_subnet.public_subnet_2.id}"]
  security_groups = ["${aws_security_group.load_balancer.id}"]
  internal = false

  tags {

    Name = "${var.global_prefix}-rest-proxy"

  }

}

resource "aws_alb_listener" "rest_proxy_listener" {

  count = "${var.instance_count["rest_proxy"] >= 1 ? 1 : 0}"
  
  load_balancer_arn = "${aws_alb.rest_proxy.arn}"  
  protocol = "HTTP"
  port = "80"
  
  default_action {

    target_group_arn = "${aws_alb_target_group.rest_proxy_target_group.arn}"
    type = "forward"

  }

}

###########################################
########### Kafka Connect LBR #############
###########################################

resource "aws_alb_target_group" "kafka_connect_target_group" {

  count = "${var.instance_count["kafka_connect"] >= 1 ? 1 : 0}"

  name = "${var.global_prefix}-kc-target-group"
  port = "8083"
  protocol = "HTTP"
  vpc_id = "${aws_vpc.default.id}"

  health_check {    

    healthy_threshold = 3    
    unhealthy_threshold = 3    
    timeout = 3   
    interval = 5    
    path = "/"
    port = "8083"

  }

}

resource "aws_alb_target_group_attachment" "kafka_connect_attachment" {

  count = "${var.instance_count["kafka_connect"] >= 1
           ? var.instance_count["kafka_connect"] : 0}"

  target_group_arn = "${aws_alb_target_group.kafka_connect_target_group.arn}"
  target_id = "${element(aws_instance.kafka_connect.*.id, count.index)}"
  port = 8083

}

resource "aws_alb" "kafka_connect" {

  depends_on = ["aws_instance.kafka_connect"]
  count = "${var.instance_count["kafka_connect"] >= 1 ? 1 : 0}"

  name = "${var.global_prefix}-kafka-connect"
  subnets = ["${aws_subnet.public_subnet_1.id}", "${aws_subnet.public_subnet_2.id}"]
  security_groups = ["${aws_security_group.load_balancer.id}"]
  internal = false

  tags {

    Name = "${var.global_prefix}-kafka-connect"

  }

}

resource "aws_alb_listener" "kafka_connect_listener" {

  count = "${var.instance_count["kafka_connect"] >= 1 ? 1 : 0}"
  
  load_balancer_arn = "${aws_alb.kafka_connect.arn}"  
  protocol = "HTTP"
  port = "80"
  
  default_action {

    target_group_arn = "${aws_alb_target_group.kafka_connect_target_group.arn}"
    type = "forward"

  }

}

###########################################
############# KSQL Server LBR #############
###########################################

resource "aws_alb_target_group" "ksql_server_target_group" {

  count = "${var.instance_count["ksql_server"] >= 1 ? 1 : 0}"

  name = "${var.global_prefix}-ks-target-group"  
  port = "8088"
  protocol = "HTTP"
  vpc_id = "${aws_vpc.default.id}"

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

  count = "${var.instance_count["ksql_server"] >= 1
           ? var.instance_count["ksql_server"] : 0}"

  target_group_arn = "${aws_alb_target_group.ksql_server_target_group.arn}"
  target_id = "${element(aws_instance.ksql_server.*.id, count.index)}"
  port = 8088

}

resource "aws_alb" "ksql_server" {

  depends_on = ["aws_instance.ksql_server"]
  count = "${var.instance_count["ksql_server"] >= 1 ? 1 : 0}"

  name = "${var.global_prefix}-ksql-server"
  subnets = ["${aws_subnet.public_subnet_1.id}", "${aws_subnet.public_subnet_2.id}"]
  security_groups = ["${aws_security_group.load_balancer.id}"]
  internal = false

  tags {

    Name = "${var.global_prefix}-ksql-server"

  }

}

resource "aws_alb_listener" "ksql_server_listener" {

  count = "${var.instance_count["ksql_server"] >= 1 ? 1 : 0}"
  
  load_balancer_arn = "${aws_alb.ksql_server.arn}"
  protocol = "HTTP"
  port = "80"
  
  default_action {

    target_group_arn = "${aws_alb_target_group.ksql_server_target_group.arn}"
    type = "forward"

  }

}

###########################################
########### Control Center LBR ############
###########################################

resource "aws_alb_target_group" "control_center_target_group" {

  count = "${var.instance_count["control_center"] >= 1 ? 1 : 0}"

  name = "${var.global_prefix}-cc-target-group"  
  port = "9021"
  protocol = "HTTP"
  vpc_id = "${aws_vpc.default.id}"

  health_check {    

    healthy_threshold = 3    
    unhealthy_threshold = 3    
    timeout = 3   
    interval = 5    
    path = "/"
    port = "9021"

  }

}

resource "aws_alb_target_group_attachment" "control_center_attachment" {

  count = "${var.instance_count["control_center"] >= 1
           ? var.instance_count["control_center"] : 0}"

  target_group_arn = "${aws_alb_target_group.control_center_target_group.arn}"
  target_id = "${element(aws_instance.control_center.*.id, count.index)}"
  port = 9021

}

resource "aws_alb" "control_center" {

  depends_on = ["aws_instance.control_center"]
  count = "${var.instance_count["control_center"] >= 1 ? 1 : 0}"

  name = "${var.global_prefix}-control-center"
  subnets = ["${aws_subnet.public_subnet_1.id}", "${aws_subnet.public_subnet_2.id}"]
  security_groups = ["${aws_security_group.load_balancer.id}"]
  internal = false

  tags {

    Name = "${var.global_prefix}-control-center"

  }

}

resource "aws_alb_listener" "control_center_listener" {

  count = "${var.instance_count["control_center"] >= 1 ? 1 : 0}"
  
  load_balancer_arn = "${aws_alb.control_center.arn}"  
  protocol = "HTTP"
  port = "80"
  
  default_action {

    target_group_arn = "${aws_alb_target_group.control_center_target_group.arn}"
    type = "forward"

  }

}

###########################################
########### Jaeger Server LBR #############
###########################################

resource "aws_alb_target_group" "jaeger_server_target_group" {

  name = "${var.global_prefix}-js-target-group"  
  port = "16686"
  protocol = "HTTP"
  vpc_id = "${aws_vpc.default.id}"

  health_check {    

    healthy_threshold = 3    
    unhealthy_threshold = 3    
    timeout = 3   
    interval = 5    
    path = "/"
    port = "16686"

  }

}

resource "aws_alb_target_group_attachment" "jaeger_server_attachment" {

  count = "${var.instance_count["jaeger_server"] >= 1
           ? var.instance_count["jaeger_server"] : 1}"

  target_group_arn = "${aws_alb_target_group.jaeger_server_target_group.arn}"
  target_id = "${element(aws_instance.jaeger_server.*.id, count.index)}"
  port = 16686

}

resource "aws_alb" "jaeger_server" {

  depends_on = ["aws_instance.jaeger_server"]

  name = "${var.global_prefix}-jaeger-server"
  subnets = ["${aws_subnet.public_subnet_1.id}", "${aws_subnet.public_subnet_2.id}"]
  security_groups = ["${aws_security_group.load_balancer.id}"]
  internal = false

  tags {

    Name = "${var.global_prefix}-jaeger-server"

  }

}

resource "aws_alb_listener" "jaeger_server_listener" {

  load_balancer_arn = "${aws_alb.jaeger_server.arn}"
  protocol = "HTTP"
  port = "80"
  
  default_action {

    target_group_arn = "${aws_alb_target_group.jaeger_server_target_group.arn}"
    type = "forward"

  }

}