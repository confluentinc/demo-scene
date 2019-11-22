###########################################
################# Outputs #################
###########################################

output "Pacman" {
  value = "http://${aws_s3_bucket.pacman.website_endpoint}"
}

output "KSQL_Server" {
  value = var.instance_count["ksql_server"] >= 1 ? join(",", formatlist("http://%s", aws_alb.ksql_server.*.dns_name)) : "KSQL Server has been disabled"
}
