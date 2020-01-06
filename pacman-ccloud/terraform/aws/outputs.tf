###########################################
################# Outputs #################
###########################################

output "Pacman" {
  value = "http://${aws_s3_bucket.pacman.website_endpoint}"
}

output "ksqlDB" {
  value = "http://${aws_alb.ksqldb_server.dns_name}"
}
