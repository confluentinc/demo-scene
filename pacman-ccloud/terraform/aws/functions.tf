###########################################
############ Common Artifacts #############
###########################################

resource "null_resource" "build_functions" {
  provisioner "local-exec" {
    command = "sh build.sh"
    interpreter = ["bash", "-c"]
    working_dir = "functions"
  }
}

data "template_file" "generic_wake_up" {
  template = file("functions/generic-wake-up.json")
}

###########################################
########### Event Handler API #############
###########################################

resource "aws_api_gateway_rest_api" "event_handler_api" {
  depends_on = [aws_lambda_function.event_handler_function]
  name = "event_handler_api"
  description = "Event Handler API"
  endpoint_configuration {
    types = ["REGIONAL"]
  }
}

resource "aws_api_gateway_resource" "event_handler_resource" {
  rest_api_id = aws_api_gateway_rest_api.event_handler_api.id
  parent_id = aws_api_gateway_rest_api.event_handler_api.root_resource_id
  path_part = "event"
}

resource "aws_api_gateway_method" "event_handler_post_method" {
  rest_api_id = aws_api_gateway_rest_api.event_handler_api.id
  resource_id = aws_api_gateway_resource.event_handler_resource.id
  http_method = "POST"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "event_handler_post_integration" {
  rest_api_id = aws_api_gateway_rest_api.event_handler_api.id
  resource_id = aws_api_gateway_resource.event_handler_resource.id
  http_method = aws_api_gateway_method.event_handler_post_method.http_method
  integration_http_method = aws_api_gateway_method.event_handler_post_method.http_method
  uri = aws_lambda_function.event_handler_function.invoke_arn
  type = "AWS_PROXY"
}

resource "aws_api_gateway_method_response" "event_handler_post_method_response" {
  rest_api_id = aws_api_gateway_rest_api.event_handler_api.id
  resource_id = aws_api_gateway_resource.event_handler_resource.id
  http_method = aws_api_gateway_method.event_handler_post_method.http_method
  status_code = "200"
}

resource "aws_api_gateway_deployment" "event_handler_v1" {
  depends_on = [aws_api_gateway_integration.event_handler_post_integration]
  rest_api_id = aws_api_gateway_rest_api.event_handler_api.id
  stage_name = "v1"
}

###########################################
########### Event Handler CORS ############
###########################################

resource "aws_api_gateway_method" "event_handler_options_method" {
  rest_api_id = aws_api_gateway_rest_api.event_handler_api.id
  resource_id = aws_api_gateway_resource.event_handler_resource.id
  http_method = "OPTIONS"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "event_handler_options_integration" {
  rest_api_id = aws_api_gateway_rest_api.event_handler_api.id
  resource_id = aws_api_gateway_resource.event_handler_resource.id
  http_method = aws_api_gateway_method.event_handler_options_method.http_method
  type = "MOCK"
  request_templates = {
    "application/json" = <<EOF
{ "statusCode": 200 }
EOF
  }
}

resource "aws_api_gateway_method_response" "event_handler_options_method_response" {
  rest_api_id = aws_api_gateway_rest_api.event_handler_api.id
  resource_id = aws_api_gateway_resource.event_handler_resource.id
  http_method = aws_api_gateway_method.event_handler_options_method.http_method
  status_code = "200"
  response_models = {
    "application/json" = "Empty"
  }
  response_parameters = {
    "method.response.header.Access-Control-Allow-Headers" = true
    "method.response.header.Access-Control-Allow-Methods" = true
    "method.response.header.Access-Control-Allow-Origin" = true
  }
}

resource "aws_api_gateway_integration_response" "event_handler_options_integration_response" {
  depends_on = [aws_s3_bucket.pacman]
  rest_api_id = aws_api_gateway_rest_api.event_handler_api.id
  resource_id = aws_api_gateway_resource.event_handler_resource.id
  http_method = aws_api_gateway_method.event_handler_options_method.http_method
  status_code = aws_api_gateway_method_response.event_handler_options_method_response.status_code
  response_parameters = {
    "method.response.header.Access-Control-Allow-Headers" = "'*'"
    "method.response.header.Access-Control-Allow-Methods" = "'POST'"
    "method.response.header.Access-Control-Allow-Origin" = "'http://${aws_s3_bucket.pacman.website_endpoint}'"
  }
}

###########################################
######### Event Handler Function ##########
###########################################

resource "aws_iam_role_policy" "event_handler_role_policy" {
  role = aws_iam_role.event_handler_role.name
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

resource "aws_iam_role" "event_handler_role" {
  name = "event_handler_role"
  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Effect": "Allow"
    }
  ]
}
EOF
}

resource "aws_lambda_function" "event_handler_function" {
  depends_on = [
    null_resource.build_functions,
    aws_iam_role.event_handler_role,
    aws_s3_bucket.pacman]
  function_name = "event_handler"
  description = "Backend function for the Event Handler API"
  filename = "functions/deploy/aws-functions-1.0.jar"
  handler = "io.confluent.cloud.pacman.EventHandler"
  role = aws_iam_role.event_handler_role.arn
  runtime = "java11"
  memory_size = 256
  timeout = 60
  environment {
    variables = {
      BOOTSTRAP_SERVERS = var.bootstrap_server
      CLUSTER_API_KEY = var.cluster_api_key
      CLUSTER_API_SECRET = var.cluster_api_secret
      ORIGIN_ALLOWED = "http://${aws_s3_bucket.pacman.website_endpoint}"
    }
  }
}

resource "aws_lambda_permission" "event_handler_api_gateway_trigger" {
  statement_id = "AllowExecutionFromApiGateway"
  action = "lambda:InvokeFunction"
  principal = "apigateway.amazonaws.com"
  function_name = aws_lambda_function.event_handler_function.function_name
  source_arn = "${aws_api_gateway_rest_api.event_handler_api.execution_arn}/${aws_api_gateway_deployment.event_handler_v1.stage_name}/*/*"
}

resource "aws_lambda_permission" "event_handler_cloudwatch_trigger" {
  statement_id = "AllowExecutionFromCloudWatch"
  action = "lambda:InvokeFunction"
  principal = "events.amazonaws.com"
  function_name = aws_lambda_function.event_handler_function.function_name
  source_arn = aws_cloudwatch_event_rule.event_handler_every_minute.arn
}

resource "aws_cloudwatch_event_rule" "event_handler_every_minute" {
  name = "execute-event-handler-every-minute"
  description = "Execute the event handler function every minute"
  schedule_expression = "rate(1 minute)"
}

resource "aws_cloudwatch_event_target" "event_handler_every_minute" {
  rule = aws_cloudwatch_event_rule.event_handler_every_minute.name
  target_id = aws_lambda_function.event_handler_function.function_name
  arn = aws_lambda_function.event_handler_function.arn
  input = data.template_file.generic_wake_up.rendered
}

###########################################
############# Scoreboard API ##############
###########################################

resource "aws_api_gateway_rest_api" "scoreboard_api" {
  depends_on = [aws_lambda_function.scoreboard_function]
  name = "scoreboard_api"
  description = "Scoreboard API"
  endpoint_configuration {
    types = ["REGIONAL"]
  }
}

resource "aws_api_gateway_resource" "scoreboard_resource" {
  rest_api_id = aws_api_gateway_rest_api.scoreboard_api.id
  parent_id = aws_api_gateway_rest_api.scoreboard_api.root_resource_id
  path_part = "scoreboard"
}

resource "aws_api_gateway_method" "scoreboard_get_method" {
  rest_api_id = aws_api_gateway_rest_api.scoreboard_api.id
  resource_id = aws_api_gateway_resource.scoreboard_resource.id
  http_method = "POST"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "scoreboard_get_integration" {
  rest_api_id = aws_api_gateway_rest_api.scoreboard_api.id
  resource_id = aws_api_gateway_resource.scoreboard_resource.id
  http_method = aws_api_gateway_method.scoreboard_get_method.http_method
  integration_http_method = aws_api_gateway_method.scoreboard_get_method.http_method
  uri = aws_lambda_function.scoreboard_function.invoke_arn
  type = "AWS_PROXY"
}

resource "aws_api_gateway_method_response" "scoreboard_get_method_response" {
  rest_api_id = aws_api_gateway_rest_api.scoreboard_api.id
  resource_id = aws_api_gateway_resource.scoreboard_resource.id
  http_method = aws_api_gateway_method.scoreboard_get_method.http_method
  status_code = "200"
}

resource "aws_api_gateway_deployment" "scoreboard_v1" {
  depends_on = [aws_api_gateway_integration.scoreboard_get_integration]
  rest_api_id = aws_api_gateway_rest_api.scoreboard_api.id
  stage_name = "v1"
}

###########################################
############# Scoreboard CORS #############
###########################################

resource "aws_api_gateway_method" "scoreboard_options_method" {
  rest_api_id = aws_api_gateway_rest_api.scoreboard_api.id
  resource_id = aws_api_gateway_resource.scoreboard_resource.id
  http_method = "OPTIONS"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "scoreboard_options_integration" {
  rest_api_id = aws_api_gateway_rest_api.scoreboard_api.id
  resource_id = aws_api_gateway_resource.scoreboard_resource.id
  http_method = aws_api_gateway_method.scoreboard_options_method.http_method
  type = "MOCK"
  request_templates = {
    "application/json" = <<EOF
{ "statusCode": 200 }
EOF
  }
}

resource "aws_api_gateway_method_response" "scoreboard_options_method_response" {
  rest_api_id = aws_api_gateway_rest_api.scoreboard_api.id
  resource_id = aws_api_gateway_resource.scoreboard_resource.id
  http_method = aws_api_gateway_method.scoreboard_options_method.http_method
  status_code = "200"
  response_models = {
    "application/json" = "Empty"
  }
  response_parameters = {
    "method.response.header.Access-Control-Allow-Headers" = true
    "method.response.header.Access-Control-Allow-Methods" = true
    "method.response.header.Access-Control-Allow-Origin" = true
  }
}

resource "aws_api_gateway_integration_response" "highest_score_options_integration_response" {
  depends_on = [aws_s3_bucket.pacman]
  rest_api_id = aws_api_gateway_rest_api.scoreboard_api.id
  resource_id = aws_api_gateway_resource.scoreboard_resource.id
  http_method = aws_api_gateway_method.scoreboard_options_method.http_method
  status_code = aws_api_gateway_method_response.scoreboard_options_method_response.status_code
  response_parameters = {
    "method.response.header.Access-Control-Allow-Headers" = "'*'"
    "method.response.header.Access-Control-Allow-Methods" = "'POST'"
    "method.response.header.Access-Control-Allow-Origin" = "'http://${aws_s3_bucket.pacman.website_endpoint}'"
  }
}

###########################################
########### Scoreboard Function ###########
###########################################

resource "aws_iam_role_policy" "scoreboard_role_policy" {
  role = aws_iam_role.scoreboard_role.name
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
    },
    {
      "Effect": "Allow",
      "Action": [
        "ec2:CreateNetworkInterface",
        "ec2:DescribeDhcpOptions",
        "ec2:DescribeNetworkInterfaces",
        "ec2:DeleteNetworkInterface",
        "ec2:DescribeSubnets",
        "ec2:DescribeSecurityGroups",
        "ec2:DescribeVpcs"
      ],
      "Resource": "*"
    }
  ]
}
POLICY
}

resource "aws_iam_role" "scoreboard_role" {
  name = "scoreboard_role"
  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Effect": "Allow"
    }
  ]
}
EOF
}

resource "aws_lambda_function" "scoreboard_function" {
  depends_on = [
    null_resource.build_functions,
    aws_iam_role.scoreboard_role,
    aws_elasticache_replication_group.cache_server,
    aws_s3_bucket.pacman]
  function_name = "scoreboard"
  description = "Backend function for the Scoreboard API"
  filename = "functions/deploy/aws-functions-1.0.jar"
  handler = "io.confluent.cloud.pacman.Scoreboard"
  role = aws_iam_role.scoreboard_role.arn
  runtime = "java11"
  memory_size = 512
  timeout = 300
  environment {
    variables = {
      ORIGIN_ALLOWED = "http://${aws_s3_bucket.pacman.website_endpoint}"
      CACHE_SERVER_HOST = aws_elasticache_replication_group.cache_server.primary_endpoint_address
      CACHE_SERVER_PORT = aws_elasticache_replication_group.cache_server.port
    }
  }
  vpc_config {
    security_group_ids = [aws_security_group.cache_server.id]
    subnet_ids = aws_subnet.private_subnet[*].id
  }
}

resource "aws_lambda_permission" "scoreboard_api_gateway_trigger" {
  statement_id = "AllowExecutionFromApiGateway"
  action = "lambda:InvokeFunction"
  principal = "apigateway.amazonaws.com"
  function_name = aws_lambda_function.scoreboard_function.function_name
  source_arn = "${aws_api_gateway_rest_api.scoreboard_api.execution_arn}/${aws_api_gateway_deployment.scoreboard_v1.stage_name}/*/*"
}

resource "aws_lambda_permission" "scoreboard_cloudwatch_trigger" {
  statement_id = "AllowExecutionFromCloudWatch"
  action = "lambda:InvokeFunction"
  principal = "events.amazonaws.com"
  function_name = aws_lambda_function.scoreboard_function.function_name
  source_arn = aws_cloudwatch_event_rule.scoreboard_every_minute.arn
}

resource "aws_cloudwatch_event_rule" "scoreboard_every_minute" {
  name = "execute-scoreboard-every-minute"
  description = "Execute the scoreboard function every minute"
  schedule_expression = "rate(1 minute)"
}

resource "aws_cloudwatch_event_target" "scoreboard_every_minute" {
  rule = aws_cloudwatch_event_rule.scoreboard_every_minute.name
  target_id = aws_lambda_function.scoreboard_function.function_name
  arn = aws_lambda_function.scoreboard_function.arn
  input = data.template_file.generic_wake_up.rendered
}

###########################################
######### Alexa Handler Function ##########
###########################################

data "template_file" "alexa_wake_up" {
  template = file("functions/alexa-wake-up.json")
}

resource "aws_iam_role_policy" "alexa_handler_role_policy" {
  count = var.alexa_enabled == true ? 1 : 0
  role = aws_iam_role.alexa_handler_role[0].name
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
    },
    {
      "Effect": "Allow",
      "Action": [
        "ec2:CreateNetworkInterface",
        "ec2:DescribeDhcpOptions",
        "ec2:DescribeNetworkInterfaces",
        "ec2:DeleteNetworkInterface",
        "ec2:DescribeSubnets",
        "ec2:DescribeSecurityGroups",
        "ec2:DescribeVpcs"
      ],
      "Resource": "*"
    }
  ]
}
POLICY
}

resource "aws_iam_role" "alexa_handler_role" {
  count = var.alexa_enabled == true ? 1 : 0
  name = "alexa_handler_role"
  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Effect": "Allow"
    }
  ]
}
EOF
}

resource "aws_lambda_function" "alexa_handler_function" {
  count = var.alexa_enabled == true ? 1 : 0
  depends_on = [
    null_resource.build_functions,
    aws_iam_role.alexa_handler_role,
    aws_elasticache_replication_group.cache_server]
  function_name = "alexa_handler"
  description = "Backend function for the Alexa Handler API"
  filename = "functions/deploy/aws-functions-1.0.jar"
  handler = "io.confluent.cloud.pacman.AlexaHandler"
  role = aws_iam_role.alexa_handler_role[0].arn
  runtime = "java11"
  memory_size = 512
  timeout = 300
  environment {
    variables = {
      CACHE_SERVER_HOST = aws_elasticache_replication_group.cache_server.primary_endpoint_address
      CACHE_SERVER_PORT = aws_elasticache_replication_group.cache_server.port
    }
  }
  vpc_config {
    security_group_ids = [aws_security_group.cache_server.id]
    subnet_ids = aws_subnet.private_subnet[*].id
  }
}

resource "aws_lambda_permission" "alexa_handler_players_trigger" {
  count = var.alexa_enabled == true ? 1 : 0
  statement_id = "AllowExecutionFromAlexaPlayers"
  action = "lambda:InvokeFunction"
  function_name = aws_lambda_function.alexa_handler_function[0].function_name
  principal = "alexa-appkit.amazon.com"
  event_source_token = var.pacman_players_skill_id
}

resource "aws_lambda_permission" "alexa_handler_details_trigger" {
  count = var.alexa_enabled == true ? 1 : 0
  statement_id = "AllowExecutionFromAlexaDetails"
  action = "lambda:InvokeFunction"
  function_name = aws_lambda_function.alexa_handler_function[0].function_name
  principal = "alexa-appkit.amazon.com"
  event_source_token = var.pacman_details_skill_id
}

resource "aws_lambda_permission" "alexa_winner_cloudwatch_trigger" {
  count = var.alexa_enabled == true ? 1 : 0
  statement_id = "AllowExecutionFromCloudWatch"
  action = "lambda:InvokeFunction"
  principal = "events.amazonaws.com"
  function_name = aws_lambda_function.alexa_handler_function[0].function_name
  source_arn = aws_cloudwatch_event_rule.alexa_handler_every_minute[0].arn
}

resource "aws_cloudwatch_event_rule" "alexa_handler_every_minute" {
  count = var.alexa_enabled == true ? 1 : 0
  name = "execute-alexa-handler-every-minute"
  description = "Execute the alexa handler function every minute"
  schedule_expression = "rate(1 minute)"
}

resource "aws_cloudwatch_event_target" "alexa_handler_every_minute" {
  count = var.alexa_enabled == true ? 1 : 0
  rule = aws_cloudwatch_event_rule.alexa_handler_every_minute[0].name
  target_id = aws_lambda_function.alexa_handler_function[0].function_name
  arn = aws_lambda_function.alexa_handler_function[0].arn
  input = data.template_file.alexa_wake_up.rendered
}
