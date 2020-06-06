###########################################
############# REST Proxy LBR ##############
###########################################

resource "google_compute_global_address" "rest_proxy" {
  count = var.instance_count["rest_proxy"] > 0 ? var.instance_count["rest_proxy"] : 0
  name = "${var.global_prefix}-rest-proxy-global-address"
}

resource "google_compute_global_forwarding_rule" "rest_proxy" {
  count = var.instance_count["rest_proxy"] > 0 ? var.instance_count["rest_proxy"] : 0
  name = "${var.global_prefix}-rest-proxy-global-forwarding-rule"
  target = google_compute_target_http_proxy.rest_proxy[0].self_link
  ip_address = google_compute_global_address.rest_proxy[0].self_link
  port_range = "80"
}

resource "google_compute_target_http_proxy" "rest_proxy" {
  count = var.instance_count["rest_proxy"] > 0 ? var.instance_count["rest_proxy"] : 0
  name = "${var.global_prefix}-rest-proxy-http-proxy"
  url_map = google_compute_url_map.rest_proxy[0].self_link
}

resource "google_compute_url_map" "rest_proxy" {
  count = var.instance_count["rest_proxy"] > 0 ? var.instance_count["rest_proxy"] : 0
  name = "${var.global_prefix}-rest-proxy-url-map"
  default_service = google_compute_backend_service.rest_proxy[0].self_link
}

resource "google_compute_backend_service" "rest_proxy" {
  count = var.instance_count["rest_proxy"] > 0 ? var.instance_count["rest_proxy"] : 0
  name = "${var.global_prefix}-rest-proxy-backend-service"
  port_name = "http"
  protocol = "HTTP"
  timeout_sec = 5
  backend {
    group = google_compute_region_instance_group_manager.rest_proxy[0].instance_group
  }
  health_checks = [google_compute_http_health_check.rest_proxy[0].self_link]
}

resource "google_compute_region_instance_group_manager" "rest_proxy" {
  count = var.instance_count["rest_proxy"] > 0 ? var.instance_count["rest_proxy"] : 0
  name = "${var.global_prefix}-rest-proxy-instance-group"
  instance_template = google_compute_instance_template.rest_proxy[0].self_link
  base_instance_name = "rest-proxy"
  region = local.region
  distribution_policy_zones = data.google_compute_zones.available.names
  target_size = var.instance_count["rest_proxy"]
  named_port {
    name = "http"
    port = 8082
  }
}

resource "google_compute_http_health_check" "rest_proxy" {
  count = var.instance_count["rest_proxy"] > 0 ? var.instance_count["rest_proxy"] : 0
  name = "${var.global_prefix}-rest-proxy-http-health-check"
  request_path = "/"
  port = "8082"
  healthy_threshold = 3
  unhealthy_threshold = 3
  check_interval_sec = 5
  timeout_sec = 3
}

resource "google_compute_instance_template" "rest_proxy" {
  count = var.instance_count["rest_proxy"] > 0 ? var.instance_count["rest_proxy"] : 0
  name = "${var.global_prefix}-rest-proxy-template"
  machine_type = "n1-standard-8"
  metadata_startup_script = data.template_file.rest_proxy_bootstrap.rendered
  disk {
    source_image = "centos-7"
    disk_size_gb = 100
  }
  network_interface {
    subnetwork = google_compute_subnetwork.private_subnet.self_link
    access_config {
    }
  }
  tags = ["${var.global_prefix}-rest-proxy"]
}

###########################################
############ KSQL Server LBR ##############
###########################################

resource "google_compute_global_address" "ksql_server" {
  count = var.instance_count["ksql_server"] > 0 ? var.instance_count["ksql_server"] : 0
  name = "${var.global_prefix}-ksql-server-global-address"
}

resource "google_compute_global_forwarding_rule" "ksql_server" {
  count = var.instance_count["ksql_server"] > 0 ? var.instance_count["ksql_server"] : 0
  name = "${var.global_prefix}-ksql-server-global-forwarding-rule"
  target = google_compute_target_http_proxy.ksql_server[0].self_link
  ip_address = google_compute_global_address.ksql_server[0].self_link
  port_range = "80"
}

resource "google_compute_target_http_proxy" "ksql_server" {
  count = var.instance_count["ksql_server"] > 0 ? var.instance_count["ksql_server"] : 0
  name = "${var.global_prefix}-ksql-server-http-proxy"
  url_map = google_compute_url_map.ksql_server[0].self_link
}

resource "google_compute_url_map" "ksql_server" {
  count = var.instance_count["ksql_server"] > 0 ? var.instance_count["ksql_server"] : 0
  name = "${var.global_prefix}-ksql-server-url-map"
  default_service = google_compute_backend_service.ksql_server[0].self_link
}

resource "google_compute_backend_service" "ksql_server" {
  count = var.instance_count["ksql_server"] > 0 ? var.instance_count["ksql_server"] : 0
  name = "${var.global_prefix}-ksql-server-backend-service"
  port_name = "http"
  protocol = "HTTP"
  timeout_sec = 5
  backend {
    group = google_compute_region_instance_group_manager.ksql_server[0].instance_group
  }
  health_checks = [google_compute_http_health_check.ksql_server[0].self_link]
}

resource "google_compute_region_instance_group_manager" "ksql_server" {
  count = var.instance_count["ksql_server"] > 0 ? var.instance_count["ksql_server"] : 0
  name = "${var.global_prefix}-ksql-server-instance-group"
  instance_template = google_compute_instance_template.ksql_server[0].self_link
  base_instance_name = "ksql-server"
  region = local.region
  distribution_policy_zones = data.google_compute_zones.available.names
  target_size = var.instance_count["ksql_server"]
  named_port {
    name = "http"
    port = 8088
  }
}

resource "google_compute_http_health_check" "ksql_server" {
  count = var.instance_count["ksql_server"] > 0 ? var.instance_count["ksql_server"] : 0
  name = "${var.global_prefix}-ksql-server-http-health-check"
  request_path = "/info"
  port = "8088"
  healthy_threshold = 3
  unhealthy_threshold = 3
  check_interval_sec = 5
  timeout_sec = 3
}

resource "google_compute_instance_template" "ksql_server" {
  count = var.instance_count["ksql_server"] > 0 ? var.instance_count["ksql_server"] : 0
  name = "${var.global_prefix}-ksql-server-template"
  machine_type = "n1-standard-8"
  metadata_startup_script = data.template_file.ksql_server_bootstrap.rendered
  disk {
    source_image = "centos-7"
    disk_size_gb = 300
  }
  network_interface {
    subnetwork = google_compute_subnetwork.private_subnet.self_link
    access_config {
    }
  }
  tags = ["${var.global_prefix}-ksql-server"]
}
