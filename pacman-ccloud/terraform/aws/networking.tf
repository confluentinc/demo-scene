###########################################
################### VPC ###################
###########################################

resource "aws_vpc" "default" {
  cidr_block = "10.0.0.0/16"
  enable_dns_hostnames = true
  tags = {
    Name = var.global_prefix
  }
}

resource "aws_internet_gateway" "default" {
  vpc_id = aws_vpc.default.id
  tags = {
    Name = var.global_prefix
  }
}

resource "aws_eip" "default" {
  depends_on = [aws_internet_gateway.default]
  vpc = true
  tags = {
    Name = var.global_prefix
  }
}

resource "aws_nat_gateway" "default" {
  depends_on = [aws_internet_gateway.default]
  allocation_id = aws_eip.default.id
  subnet_id = aws_subnet.public_subnet[0].id
  tags = {
    Name = var.global_prefix
  }
}

resource "aws_route" "default" {
  route_table_id = aws_vpc.default.main_route_table_id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id = aws_internet_gateway.default.id
}

resource "aws_route_table" "private_route_table" {
  vpc_id = aws_vpc.default.id
  tags = {
    Name = "${var.global_prefix}-private-route-table"
  }
}

resource "aws_route" "private_route_2_internet" {
  route_table_id = aws_route_table.private_route_table.id
  destination_cidr_block = "0.0.0.0/0"
  nat_gateway_id = aws_nat_gateway.default.id
}

resource "aws_route_table_association" "private_subnet_association" {
  count = length(data.aws_availability_zones.available.names)
  subnet_id = element(aws_subnet.private_subnet.*.id, count.index)
  route_table_id = aws_route_table.private_route_table.id
}

resource "aws_route_table_association" "public_subnet_association" {
  count = length(data.aws_availability_zones.available.names)
  subnet_id = element(aws_subnet.public_subnet.*.id, count.index)
  route_table_id = aws_vpc.default.main_route_table_id
}

###########################################
################# Subnets #################
###########################################

variable "private_cidr_blocks" {
  type = list(string)
  default = [
    "10.0.1.0/24",
    "10.0.2.0/24",
    "10.0.3.0/24",
    "10.0.4.0/24",
    "10.0.5.0/24",
    "10.0.6.0/24",
    "10.0.7.0/24",
    "10.0.8.0/24",
  ]
}

variable "public_cidr_blocks" {
  type = list(string)
  default = [
    "10.0.9.0/24",
    "10.0.10.0/24",
    "10.0.11.0/24",
    "10.0.12.0/24",
    "10.0.13.0/24",
    "10.0.14.0/24",
    "10.0.15.0/24",
    "10.0.16.0/24",
  ]
}

resource "aws_subnet" "private_subnet" {
  count = length(data.aws_availability_zones.available.names)
  vpc_id = aws_vpc.default.id
  cidr_block = element(var.private_cidr_blocks, count.index)
  map_public_ip_on_launch = false
  availability_zone = data.aws_availability_zones.available.names[count.index]
  tags = {
    Name = "${var.global_prefix}-private-subnet-${count.index}"
  }
}

resource "aws_subnet" "public_subnet" {
  count = length(data.aws_availability_zones.available.names)
  vpc_id = aws_vpc.default.id
  cidr_block = element(var.public_cidr_blocks, count.index)
  map_public_ip_on_launch = true
  availability_zone = data.aws_availability_zones.available.names[count.index]
  tags = {
    Name = "${var.global_prefix}-public-subnet-${count.index}"
  }
}

###########################################
############# Security Groups #############
###########################################

resource "aws_security_group" "load_balancer" {
  name = "${var.global_prefix}-load-balancer"
  description = "Load Balancer"
  vpc_id = aws_vpc.default.id
  ingress {
    from_port = 80
    to_port = 80
    protocol = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  egress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = {
    Name = "${var.global_prefix}-load-balancer"
  }
}

resource "aws_security_group" "ecs_tasks" {
  name = "${var.global_prefix}-ecs-tasks"
  description = "Inbound Access from LBR"
  vpc_id = aws_vpc.default.id
  ingress {
    protocol = "tcp"
    from_port = 8088
    to_port = 8088
    security_groups = [aws_security_group.load_balancer.id]
  }
  egress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = {
    Name = "${var.global_prefix}-ecs-tasks"
  }
}
