###########################################
############### KSQL Server ###############
###########################################

data "aws_iam_policy_document" "ksql_server_policy_document" {
  statement {
    actions = ["sts:AssumeRole"]
    sid = ""
    effect = "Allow"
    principals {
      type = "Service"
      identifiers = [
        "events.amazonaws.com",
        "ecs-tasks.amazonaws.com"
      ]
    }
  }
}

resource "aws_iam_role_policy" "ksql_server_role_policy" {
  role = aws_iam_role.ksql_server_role.name
  policy = <<POLICY
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Resource": [
        "*"
      ],
      "Action": [
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents"
      ]
    }
  ]
}
POLICY
}

resource "aws_iam_role" "ksql_server_role" {
  name = "ksql_server_role"
  assume_role_policy = data.aws_iam_policy_document.ksql_server_policy_document.json
}

resource "aws_iam_role_policy_attachment" "ksql_server_policy_attachment" {
  role = aws_iam_role.ksql_server_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_ecs_cluster" "ksql_server_cluster" {
  name = "${var.global_prefix}-ksql-server-cluster"
}

data "template_file" "ksql_server_container_def" {
  template = file("../util/ksql-server.json")
  vars = {
    bootstrap_server = var.bootstrap_server
    cluster_api_key = var.cluster_api_key
    cluster_api_secret = var.cluster_api_secret
    schema_registry_url = var.schema_registry_url
    schema_registry_basic_auth = var.schema_registry_basic_auth
    ksql_server_image = var.ksql_server_image
    logs_region = local.region
    global_prefix = var.global_prefix
  }
}

resource "aws_ecs_task_definition" "ksql_server_task_def" {
  family = "ksql_server_task_def"
  network_mode = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu = "2048"
  memory = "8192"
  execution_role_arn = aws_iam_role.ksql_server_role.arn
  task_role_arn = aws_iam_role.ksql_server_role.arn
  container_definitions = data.template_file.ksql_server_container_def.rendered
}

resource "aws_ecs_service" "ksql_server_service" {
  depends_on = [
    aws_alb_listener.listener,
    aws_nat_gateway.default
  ]
  name = "${var.global_prefix}-ksql-server-service"
  cluster = aws_ecs_cluster.ksql_server_cluster.id
  task_definition = aws_ecs_task_definition.ksql_server_task_def.arn
  desired_count = var.instance_count["ksql_server"]
  launch_type = "FARGATE"
  network_configuration {
    security_groups = [aws_security_group.ecs_tasks.id]
    subnets = aws_subnet.private_subnet[*].id
  }
  load_balancer {
    target_group_arn = aws_alb_target_group.ksql_server_target_group.id
    container_name = "ksql_server"
    container_port = "8088"
  }
}

###########################################
############# KSQL Server LBR #############
###########################################

resource "aws_alb" "ksql_server" {
  name = "pacman${random_string.random_string.result}-ksql"
  subnets = [aws_subnet.public_subnet_1.id, aws_subnet.public_subnet_2.id]
  security_groups = [aws_security_group.load_balancer.id]
  tags = {
    Name = "pacman${random_string.random_string.result}-ksql"
  }
}

resource "aws_alb_target_group" "ksql_server_target_group" {
  name = "${var.global_prefix}-ks-target-group"
  port = "8088"
  protocol = "HTTP"
  vpc_id = aws_vpc.default.id
  target_type = "ip"
  health_check {
    healthy_threshold = 3
    unhealthy_threshold = 3
    timeout = 3
    interval = 5
    path = "/info"
    port = "8088"
  }
}

resource "aws_alb_listener" "listener" {
  load_balancer_arn = aws_alb.ksql_server.arn
  protocol = "HTTP"
  port = "80"
  default_action {
    target_group_arn = aws_alb_target_group.ksql_server_target_group.arn
    type = "forward"
  }
}
