###########################################
############ Common Artifacts #############
###########################################

resource "null_resource" "build_functions" {
  # triggers = {
  #   always_run = "${timestamp()}"
  # }
  provisioner "local-exec" {
    command = "sh build.sh"
    interpreter = ["bash", "-c"]
    working_dir = "functions"
  }
}

locals {
  generic_wake_up = templatefile("functions/generic-wake-up.json", {
    cloud_provider = "AWS"
    ksqldb_endpoint = "${aws_api_gateway_deployment.event_handler_v1.invoke_url}${aws_api_gateway_resource.event_handler_resource.path}"
    ksql_basic_auth_user_info = local.ksql_basic_auth_user_info
    #TODO scoreboard_api = "${aws_api_gateway_deployment.scoreboard_v1.invoke_url}${aws_api_gateway_resource.scoreboard_resource.path}"
    scoreboard_api = ""
  })
}

###########################################
########### Event Handler API #############
###########################################

resource "aws_api_gateway_rest_api" "event_handler_api" {
  depends_on = [aws_lambda_function.event_handler_function]
  name = "${local.resource_prefix}_event_handler_api"
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
    "method.response.header.Access-Control-Allow-Methods" = "'POST,OPTIONS'"
    "method.response.header.Access-Control-Allow-Origin" = "'http://${aws_s3_bucket_website_configuration.pacman.website_endpoint}'"
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

  name = "${local.resource_prefix}_event_handler_role"
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
  function_name = "${local.resource_prefix}_event_handler"
  description = "Backend function for the Event Handler API"
  filename = "functions/deploy/streaming-pacman-1.0.jar"
  #source_code_hash = filemd5("functions/deploy/streaming-pacman-1.0.jar")
  handler = "com.gnatali.streaming.pacman.EventHandler"
  role = aws_iam_role.event_handler_role.arn
  runtime = "java11"
  memory_size = 256
  timeout = 60
  environment {
    variables = {
      ORIGIN_ALLOWED = "http://${aws_s3_bucket_website_configuration.pacman.website_endpoint}"
      KSQLDB_ENDPOINT = confluent_ksql_cluster.main.rest_endpoint
      KSQLDB_API_AUTH_INFO = local.ksql_basic_auth_user_info
    }
  }
}

resource "aws_lambda_permission" "event_handler_api_gateway_trigger" {
  statement_id = "AllowExecutionFromApiGateway"
  action = "lambda:InvokeFunction"
  principal = "apigateway.amazonaws.com"
  function_name = aws_lambda_function.event_handler_function.function_name
  source_arn = "${aws_api_gateway_rest_api.event_handler_api.execution_arn}/${aws_api_gateway_deployment.event_handler_v1.stage_name}/*"
}

resource "aws_lambda_permission" "event_handler_cloudwatch_trigger" {
  statement_id = "AllowExecutionFromCloudWatch"
  action = "lambda:InvokeFunction"
  principal = "events.amazonaws.com"
  function_name = aws_lambda_function.event_handler_function.function_name
  source_arn = aws_cloudwatch_event_rule.event_handler_every_minute.arn
}

resource "aws_cloudwatch_event_rule" "event_handler_every_minute" {
  name = "${local.resource_prefix}-execute-event-handler-every-minute"
  description = "Execute the event handler function every minute"
  schedule_expression = "rate(1 minute)"
}

resource "aws_cloudwatch_event_target" "event_handler_every_minute" {
  rule = aws_cloudwatch_event_rule.event_handler_every_minute.name
  target_id = aws_lambda_function.event_handler_function.function_name
  arn = aws_lambda_function.event_handler_function.arn
  input = local.generic_wake_up
}
