###########################################
############## Cache Server ###############
###########################################

resource "aws_elasticache_subnet_group" "cache_server" {
  name = "${var.global_prefix}-cache-server"
  subnet_ids = aws_subnet.private_subnet[*].id
  description = "${var.global_prefix}-cache-server"
}

resource "aws_elasticache_replication_group" "cache_server" {
  replication_group_id = "${var.global_prefix}-cache-server"
  replication_group_description = "Cache server for the APIs"
  subnet_group_name = aws_elasticache_subnet_group.cache_server.name
  availability_zones = data.aws_availability_zones.available.names
  number_cache_clusters = length(data.aws_availability_zones.available.names)
  security_group_ids = [aws_security_group.cache_server.id]
  automatic_failover_enabled = true
  node_type = "cache.m4.large"
  parameter_group_name = "default.redis5.0"
  port = 6379
}
