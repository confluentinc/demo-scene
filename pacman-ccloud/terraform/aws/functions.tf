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

data "template_file" "wake_up_function" {
  template = file("functions/sample.json")
}

###########################################
########### Event Handler API #############
###########################################

resource "aws_api_gateway_rest_api" "event_handler_api" {
  name = "event_handler_api"
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
    aws_iam_role.event_handler_role]
  function_name = "event_handler"
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
    source_arn = aws_cloudwatch_event_rule.event_handler_every_five_minutes.arn
}

resource "aws_cloudwatch_event_rule" "event_handler_every_five_minutes" {
    name = "execute-event-handler-every-five-minutes"
    description = "Execute the event handler function every five minutes"
    schedule_expression = "rate(5 minutes)"
}

resource "aws_cloudwatch_event_target" "event_handler_every_five_minutes" {
    rule = aws_cloudwatch_event_rule.event_handler_every_five_minutes.name
    target_id = aws_lambda_function.event_handler_function.function_name
    arn = aws_lambda_function.event_handler_function.arn
    input = data.template_file.wake_up_function.rendered
}

###########################################
########### Highest Score API #############
###########################################

resource "aws_api_gateway_rest_api" "highest_score_api" {
  name = "highest_score_api"
  endpoint_configuration {
    types = ["REGIONAL"]
  }
}

resource "aws_api_gateway_resource" "highest_score_resource" {
  rest_api_id = aws_api_gateway_rest_api.highest_score_api.id
  parent_id = aws_api_gateway_rest_api.highest_score_api.root_resource_id
  path_part = "value"
}

resource "aws_api_gateway_method" "highest_score_get_method" {
  rest_api_id = aws_api_gateway_rest_api.highest_score_api.id
  resource_id = aws_api_gateway_resource.highest_score_resource.id
  http_method = "POST"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "highest_score_get_integration" {
  rest_api_id = aws_api_gateway_rest_api.highest_score_api.id
  resource_id = aws_api_gateway_resource.highest_score_resource.id
  http_method = aws_api_gateway_method.highest_score_get_method.http_method
  integration_http_method = aws_api_gateway_method.highest_score_get_method.http_method
  uri = aws_lambda_function.highest_score_function.invoke_arn
  type = "AWS_PROXY"
}

resource "aws_api_gateway_method_response" "highest_score_get_method_response" {
  rest_api_id = aws_api_gateway_rest_api.highest_score_api.id
  resource_id = aws_api_gateway_resource.highest_score_resource.id
  http_method = aws_api_gateway_method.highest_score_get_method.http_method
  status_code = "200"
}

resource "aws_api_gateway_deployment" "highest_score_v1" {
  depends_on = [aws_api_gateway_integration.highest_score_get_integration]
  rest_api_id = aws_api_gateway_rest_api.highest_score_api.id
  stage_name = "v1"
}

###########################################
########### Highest Score CORS ############
###########################################

resource "aws_api_gateway_method" "highest_score_options_method" {
  rest_api_id = aws_api_gateway_rest_api.highest_score_api.id
  resource_id = aws_api_gateway_resource.highest_score_resource.id
  http_method = "OPTIONS"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "highest_score_options_integration" {
  rest_api_id = aws_api_gateway_rest_api.highest_score_api.id
  resource_id = aws_api_gateway_resource.highest_score_resource.id
  http_method = aws_api_gateway_method.highest_score_options_method.http_method
  type = "MOCK"
  request_templates = {
    "application/json" = <<EOF
{ "statusCode": 200 }
EOF
  }
}

resource "aws_api_gateway_method_response" "highest_score_options_method_response" {
  rest_api_id = aws_api_gateway_rest_api.highest_score_api.id
  resource_id = aws_api_gateway_resource.highest_score_resource.id
  http_method = aws_api_gateway_method.highest_score_options_method.http_method
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
  rest_api_id = aws_api_gateway_rest_api.highest_score_api.id
  resource_id = aws_api_gateway_resource.highest_score_resource.id
  http_method = aws_api_gateway_method.highest_score_options_method.http_method
  status_code = aws_api_gateway_method_response.highest_score_options_method_response.status_code
  response_parameters = {
    "method.response.header.Access-Control-Allow-Headers" = "'*'"
    "method.response.header.Access-Control-Allow-Methods" = "'POST'"
    "method.response.header.Access-Control-Allow-Origin" = "'http://${aws_s3_bucket.pacman.website_endpoint}'"
  }
}

###########################################
######### Highest Score Function ##########
###########################################

resource "aws_iam_role_policy" "highest_score_role_policy" {
  role = aws_iam_role.highest_score_role.name
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

resource "aws_iam_role" "highest_score_role" {
  name = "highest_score_role"
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

resource "aws_lambda_function" "highest_score_function" {
  depends_on = [
    null_resource.build_functions,
    aws_iam_role.event_handler_role]
  function_name = "highest_score"
  filename = "functions/deploy/aws-functions-1.0.jar"
  handler = "io.confluent.cloud.pacman.HighestScore"
  role = aws_iam_role.highest_score_role.arn
  runtime = "java11"
  memory_size = 256
  timeout = 300
  environment {
    variables = {
      BOOTSTRAP_SERVERS = var.bootstrap_server
      CLUSTER_API_KEY = var.cluster_api_key
      CLUSTER_API_SECRET = var.cluster_api_secret
      ORIGIN_ALLOWED = "http://${aws_s3_bucket.pacman.website_endpoint}"
    }
  }
}

resource "aws_lambda_permission" "highest_score_api_gateway_trigger" {
  statement_id = "AllowExecutionFromApiGateway"
  action = "lambda:InvokeFunction"
  principal = "apigateway.amazonaws.com"
  function_name = aws_lambda_function.highest_score_function.function_name
  source_arn = "${aws_api_gateway_rest_api.highest_score_api.execution_arn}/${aws_api_gateway_deployment.highest_score_v1.stage_name}/*/*"
}

resource "aws_lambda_permission" "highest_score_cloudwatch_trigger" {
    statement_id = "AllowExecutionFromCloudWatch"
    action = "lambda:InvokeFunction"
    principal = "events.amazonaws.com"
    function_name = aws_lambda_function.highest_score_function.function_name
    source_arn = aws_cloudwatch_event_rule.highest_score_every_one_minute.arn
}

resource "aws_cloudwatch_event_rule" "highest_score_every_one_minute" {
    name = "execute-highest-score-every-one-minute"
    description = "Execute the highest score function every one minute"
    schedule_expression = "rate(1 minute)"
}

resource "aws_cloudwatch_event_target" "highest_score_every_one_minute" {
    rule = aws_cloudwatch_event_rule.highest_score_every_one_minute.name
    target_id = aws_lambda_function.highest_score_function.function_name
    arn = aws_lambda_function.highest_score_function.arn
    input = data.template_file.wake_up_function.rendered
}
