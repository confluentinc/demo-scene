###########################################
################# Outputs #################
###########################################

output "Pacman" {
  value = "http://${aws_s3_bucket.pacman.website_endpoint}"
}

output "KSQL_Server" {
  value = "http://${aws_alb.ksql_server.dns_name}"
}
