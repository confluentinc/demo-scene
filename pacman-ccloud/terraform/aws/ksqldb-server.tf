###########################################
################## ksqlDB #################
###########################################

data "aws_iam_policy_document" "ksqldb_server_policy_document" {
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

resource "aws_iam_role_policy" "ksqldb_server_role_policy" {
  role = aws_iam_role.ksqldb_server_role.name
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

resource "aws_iam_role" "ksqldb_server_role" {
  name = "ksqldb_server_role"
  assume_role_policy = data.aws_iam_policy_document.ksqldb_server_policy_document.json
}

resource "aws_iam_role_policy_attachment" "ksqldb_server_policy_attachment" {
  role = aws_iam_role.ksqldb_server_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_ecs_cluster" "ksqldb_server_cluster" {
  name = "${var.global_prefix}-ksqldb-server-cluster"
}

data "template_file" "ksqldb_server_definition" {
  template = file("../util/ksqldb-server.json")
  vars = {
    bootstrap_server = var.bootstrap_server
    cluster_api_key = var.cluster_api_key
    cluster_api_secret = var.cluster_api_secret
    schema_registry_url = var.schema_registry_url
    schema_registry_basic_auth = var.schema_registry_basic_auth
    ksqldb_server_image = var.ksqldb_server_image
    logs_region = local.region
    global_prefix = var.global_prefix
    access_control_allow_origin = "http://${aws_s3_bucket.pacman.website_endpoint}"
    access_control_allow_methods = "OPTIONS,POST"
    access_control_allow_headers = "*"
  }
}

resource "aws_ecs_task_definition" "ksqldb_server_task" {
  family = "ksqldb_server_task"
  network_mode = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu = "4096"
  memory = "16384"
  execution_role_arn = aws_iam_role.ksqldb_server_role.arn
  task_role_arn = aws_iam_role.ksqldb_server_role.arn
  container_definitions = data.template_file.ksqldb_server_definition.rendered
}

resource "aws_ecs_service" "ksqldb_server_service" {
  depends_on = [
    aws_alb_listener.ksqldb_lbr_listener,
    aws_nat_gateway.default
  ]
  name = "${var.global_prefix}-ksqldb-server-service"
  cluster = aws_ecs_cluster.ksqldb_server_cluster.id
  task_definition = aws_ecs_task_definition.ksqldb_server_task.arn
  desired_count = 1
  launch_type = "FARGATE"
  network_configuration {
    security_groups = [aws_security_group.ecs_tasks.id]
    subnets = aws_subnet.private_subnet[*].id
  }
  load_balancer {
    target_group_arn = aws_alb_target_group.ksqldb_target_group.id
    container_name = "ksqldb_server"
    container_port = "8088"
  }
}

###########################################
############## Auto Scaling ###############
###########################################

resource "aws_appautoscaling_target" "ksqldb_server_auto_scaling_target" {
  service_namespace = "ecs"
  resource_id = "service/${aws_ecs_cluster.ksqldb_server_cluster.name}/${aws_ecs_service.ksqldb_server_service.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  role_arn = aws_iam_role.ksqldb_server_role.arn
  min_capacity = 1
  max_capacity = 8
}

resource "aws_appautoscaling_policy" "ksqldb_server_auto_scaling_up" {
  depends_on = [aws_appautoscaling_target.ksqldb_server_auto_scaling_target]
  name = "ksqldb_server_auto_scaling_up"
  service_namespace  = "ecs"
  resource_id = "service/${aws_ecs_cluster.ksqldb_server_cluster.name}/${aws_ecs_service.ksqldb_server_service.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  step_scaling_policy_configuration {
    adjustment_type = "ChangeInCapacity"
    cooldown = 60
    metric_aggregation_type = "Maximum"
    step_adjustment {
      metric_interval_lower_bound = 0
      scaling_adjustment = 1
    }
  }
}

resource "aws_cloudwatch_metric_alarm" "ksqldb_server_cpu_high_alarm" {
  alarm_name = "ksqldb_server_cpu_high_alarm"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods = "2"
  metric_name = "CPUUtilization"
  namespace = "AWS/ECS"
  period = "60"
  statistic = "Average"
  threshold = "85"
  dimensions = {
    ClusterName = aws_ecs_cluster.ksqldb_server_cluster.name
    ServiceName = aws_ecs_service.ksqldb_server_service.name
  }
  alarm_actions = [aws_appautoscaling_policy.ksqldb_server_auto_scaling_up.arn]
}

resource "aws_appautoscaling_policy" "ksqldb_server_auto_scaling_down" {
  depends_on = [aws_appautoscaling_target.ksqldb_server_auto_scaling_target]
  name = "ksqldb_server_auto_scaling_down"
  service_namespace  = "ecs"
  resource_id = "service/${aws_ecs_cluster.ksqldb_server_cluster.name}/${aws_ecs_service.ksqldb_server_service.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  step_scaling_policy_configuration {
    adjustment_type = "ChangeInCapacity"
    cooldown = 60
    metric_aggregation_type = "Maximum"
    step_adjustment {
      metric_interval_lower_bound = 0
      scaling_adjustment = -1
    }
  }
}

resource "aws_cloudwatch_metric_alarm" "ksqldb_server_cpu_low_alarm" {
  alarm_name = "ksqldb_server_cpu_low_alarm"
  comparison_operator = "LessThanOrEqualToThreshold"
  evaluation_periods = "2"
  metric_name = "CPUUtilization"
  namespace = "AWS/ECS"
  period = "60"
  statistic = "Average"
  threshold = "10"
  dimensions = {
    ClusterName = aws_ecs_cluster.ksqldb_server_cluster.name
    ServiceName = aws_ecs_service.ksqldb_server_service.name
  }
 alarm_actions = [aws_appautoscaling_policy.ksqldb_server_auto_scaling_down.arn]
}

###########################################
########### ksqlDB Load Balancer ##########
###########################################

resource "aws_alb" "ksqldb_lbr" {
  name = "pacman${random_string.random_string.result}-ksqldb"
  subnets = aws_subnet.public_subnet[*].id
  security_groups = [aws_security_group.load_balancer.id]
  tags = {
    Name = "pacman${random_string.random_string.result}-ksqldb"
  }
}

resource "aws_alb_target_group" "ksqldb_target_group" {
  name = "${var.global_prefix}-ksdb-target-group"
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

resource "aws_alb_listener" "ksqldb_lbr_listener" {
  load_balancer_arn = aws_alb.ksqldb_lbr.arn
  protocol = "HTTP"
  port = "80"
  default_action {
    target_group_arn = aws_alb_target_group.ksqldb_target_group.arn
    type = "forward"
  }
}
