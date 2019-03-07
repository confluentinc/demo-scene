###########################################
################### VPC ###################
###########################################

resource "aws_vpc" "default" {
    
    cidr_block = "10.0.0.0/16"

    tags {

        Name = "${var.global_prefix}"

    }

}

resource "aws_internet_gateway" "default" {

  vpc_id = "${aws_vpc.default.id}"

    tags {

        Name = "${var.global_prefix}"

    }

}

resource "aws_eip" "default" {

  depends_on = ["aws_internet_gateway.default"]
  vpc = true

    tags {

        Name = "${var.global_prefix}"

    }

}

resource "aws_nat_gateway" "default" {

    depends_on = ["aws_internet_gateway.default"]

    allocation_id = "${aws_eip.default.id}"
    subnet_id = "${aws_subnet.public_subnet_1.id}"

    tags {

        Name = "${var.global_prefix}"

    }

}

resource "aws_route" "default" {

  route_table_id = "${aws_vpc.default.main_route_table_id}"
  destination_cidr_block = "0.0.0.0/0"
  gateway_id = "${aws_internet_gateway.default.id}"

}

resource "aws_route_table" "private_route_table" {

  vpc_id = "${aws_vpc.default.id}"

  tags {

    Name = "${var.global_prefix}-private-route-table"

  }

}

resource "aws_route" "private_route_2_internet" {

  route_table_id = "${aws_route_table.private_route_table.id}"
  destination_cidr_block = "0.0.0.0/0"
  nat_gateway_id = "${aws_nat_gateway.default.id}"

}

resource "aws_route_table_association" "public_subnet_1_association" {

    subnet_id = "${aws_subnet.public_subnet_1.id}"
    route_table_id = "${aws_vpc.default.main_route_table_id}"

}

resource "aws_route_table_association" "public_subnet_2_association" {

    subnet_id = "${aws_subnet.public_subnet_2.id}"
    route_table_id = "${aws_vpc.default.main_route_table_id}"

}

resource "aws_route_table_association" "private_subnet_association" {

    count = "${length(var.aws_availability_zones)}"
    
    subnet_id = "${element(aws_subnet.private_subnet.*.id, count.index)}"
    route_table_id = "${aws_route_table.private_route_table.id}"

}

###########################################
################# Subnets #################
###########################################

variable "reserved_cidr_blocks" {

  type = "list"

  // The list below represents the possible values
  // of CIDR blocks to be used in the private subnets.
  // Since the private subnets are created dynamically,
  // we use this list to reserve six possible subnets.
  
  // Currently AWS provides ~3 availability zones per
  // region, so we are providing here 6 which may be
  // more than enough.

  default = ["10.0.1.0/24", "10.0.2.0/24", "10.0.3.0/24",
             "10.0.4.0/24", "10.0.5.0/24", "10.0.6.0/24"]

}

resource "aws_subnet" "private_subnet" {

  count = "${length(var.aws_availability_zones)}"
  vpc_id = "${aws_vpc.default.id}"
  cidr_block = "${element(var.reserved_cidr_blocks, count.index)}"
  map_public_ip_on_launch = false
  availability_zone = "${element(var.aws_availability_zones, count.index)}"

    tags {

        Name = "${var.global_prefix}-private-subnet-${count.index}"

    }    

}

resource "aws_subnet" "public_subnet_1" {

  vpc_id = "${aws_vpc.default.id}"
  cidr_block = "10.0.7.0/24"
  map_public_ip_on_launch = true
  availability_zone = "${element(var.aws_availability_zones, 0)}"

    tags {

        Name = "${var.global_prefix}-public-subnet-1"

    }    

}

resource "aws_subnet" "public_subnet_2" {

  vpc_id = "${aws_vpc.default.id}"
  cidr_block = "10.0.8.0/24"
  map_public_ip_on_launch = true
  availability_zone = "${element(var.aws_availability_zones, 1)}"

    tags {

        Name = "${var.global_prefix}-public-subnet-2"

    }    

}

resource "aws_subnet" "bastion_server" {

  count = "${var.instance_count["bastion_server"] >= 1 ? 1 : 0}"
  
  vpc_id = "${aws_vpc.default.id}"
  cidr_block = "10.0.9.0/24"
  map_public_ip_on_launch = true
  availability_zone = "${element(var.aws_availability_zones, 0)}"

    tags {

        Name = "${var.global_prefix}-bastion-server"

    }    

}

###########################################
############# Security Groups #############
###########################################

resource "aws_security_group" "load_balancer" {

  name = "${var.global_prefix}-load-balancer"
  description = "Load Balancer"
  vpc_id = "${aws_vpc.default.id}"

  ingress {

    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]

  }

  egress {

    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    
  }  

    tags {

        Name = "${var.global_prefix}-load-balancer"

    }    
  
}

resource "aws_security_group" "schema_registry" {

  count = "${var.instance_count["schema_registry"] >= 1 ? 1 : 0}"

  name = "${var.global_prefix}-schema-registry"
  description = "Schema Registry"
  vpc_id = "${aws_vpc.default.id}"

  ingress {

    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["10.0.9.0/24"]

  }

  ingress {

    from_port   = 8081
    to_port     = 8081
    protocol    = "tcp"

    cidr_blocks = ["10.0.1.0/24",
                   "10.0.2.0/24",
                   "10.0.3.0/24",
                   "10.0.4.0/24",
                   "10.0.5.0/24",
                   "10.0.6.0/24",
                   "10.0.7.0/24",
                   "10.0.8.0/24"]

  }

  egress {

    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    
  }

    tags {

        Name = "${var.global_prefix}-schema-registry"

    }    
  
}

resource "aws_security_group" "rest_proxy" {

  count = "${var.instance_count["rest_proxy"] >= 1 ? 1 : 0}"

  name = "${var.global_prefix}-rest-proxy"
  description = "REST Proxy"
  vpc_id = "${aws_vpc.default.id}"

  ingress {

    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["10.0.9.0/24"]

  }

  ingress {

    from_port   = 8082
    to_port     = 8082
    protocol    = "tcp"

    cidr_blocks = ["10.0.1.0/24",
                   "10.0.2.0/24",
                   "10.0.3.0/24",
                   "10.0.4.0/24",
                   "10.0.5.0/24",
                   "10.0.6.0/24",
                   "10.0.7.0/24",
                   "10.0.8.0/24"]

  }

  egress {

    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    
  }  

    tags {

        Name = "${var.global_prefix}-rest-proxy"

    }    
  
}

resource "aws_security_group" "kafka_connect" {

  count = "${var.instance_count["kafka_connect"] >= 1 ? 1 : 0}"

  name = "${var.global_prefix}-kafka-connect"
  description = "Kafka Connect"
  vpc_id = "${aws_vpc.default.id}"

  ingress {

    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["10.0.9.0/24"]

  }

  ingress {

    from_port   = 8083
    to_port     = 8083
    protocol    = "tcp"

    cidr_blocks = ["10.0.1.0/24",
                   "10.0.2.0/24",
                   "10.0.3.0/24",
                   "10.0.4.0/24",
                   "10.0.5.0/24",
                   "10.0.6.0/24",
                   "10.0.7.0/24",
                   "10.0.8.0/24"]

  }

  egress {

    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    
  }

    tags {

        Name = "${var.global_prefix}-kafka-connect"

    }    
  
}

resource "aws_security_group" "ksql_server" {

  count = "${var.instance_count["ksql_server"] >= 1 ? 1 : 0}"

  name = "${var.global_prefix}-ksql-server"
  description = "KSQL Server"
  vpc_id = "${aws_vpc.default.id}"

  ingress {

    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["10.0.9.0/24"]

  }

  ingress {

    from_port   = 8088
    to_port     = 8088
    protocol    = "tcp"

    cidr_blocks = ["10.0.1.0/24",
                   "10.0.2.0/24",
                   "10.0.3.0/24",
                   "10.0.4.0/24",
                   "10.0.5.0/24",
                   "10.0.6.0/24",
                   "10.0.7.0/24",
                   "10.0.8.0/24"]

  }

  egress {

    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    
  }

    tags {

        Name = "${var.global_prefix}-ksql-server"

    }    
  
}

resource "aws_security_group" "control_center" {

  count = "${var.instance_count["control_center"] >= 1 ? 1 : 0}"

  name = "${var.global_prefix}-control-center"
  description = "Control Center"
  vpc_id = "${aws_vpc.default.id}"

  ingress {

    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["10.0.9.0/24"]

  }

  ingress {

    from_port   = 9021
    to_port     = 9021
    protocol    = "tcp"

    cidr_blocks = ["10.0.1.0/24",
                   "10.0.2.0/24",
                   "10.0.3.0/24",
                   "10.0.4.0/24",
                   "10.0.5.0/24",
                   "10.0.6.0/24",
                   "10.0.7.0/24",
                   "10.0.8.0/24"]

  }

  egress {

    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    
  }

    tags {

        Name = "${var.global_prefix}-control-center"

    }    
  
}

resource "aws_security_group" "bastion_server" {

  count = "${var.instance_count["bastion_server"] >= 1 ? 1 : 0}"

  name = "${var.global_prefix}-bastion-server"
  description = "Bastion Server"
  vpc_id = "${aws_vpc.default.id}"

  ingress {

    from_port = 22
    to_port = 22
    protocol = "tcp"

    // For security reasons, it
    // is recommended to set your
    // public IP address here, so
    // the bastion server is only
    // accessible from your end.

    cidr_blocks = ["0.0.0.0/0"]

  }

  egress {

    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = ["0.0.0.0/0"]

  }

    tags {

        Name = "${var.global_prefix}-bastion-server"

    }    

}