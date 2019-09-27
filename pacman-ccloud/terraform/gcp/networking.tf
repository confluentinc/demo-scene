###########################################
################### VPC ###################
###########################################
resource "google_compute_network" "default" {
  name                    = var.global_prefix
  auto_create_subnetworks = false
}

###########################################
################# Subnets #################
###########################################

resource "google_compute_subnetwork" "private_subnet" {
  name          = "private-subnet-${var.global_prefix}"
  project       = var.gcp_project
  region        = local.region
  network       = google_compute_network.default.id
  ip_cidr_range = "10.0.1.0/24"
}

resource "google_compute_subnetwork" "public_subnet" {
  name          = "public-subnet-${var.global_prefix}"
  project       = var.gcp_project
  region        = local.region
  network       = google_compute_network.default.id
  ip_cidr_range = "10.0.2.0/24"
}

###########################################
############ Compute Firewalls ############
###########################################

resource "google_compute_firewall" "rest_proxy" {
  count = var.instance_count["rest_proxy"] >= 1 ? 1 : 0
  name    = "rest-proxy-${var.global_prefix}"
  network = google_compute_network.default.name
  allow {
    protocol = "tcp"
    ports    = ["22", "8082"]
  }
  source_ranges = ["0.0.0.0/0"]
  target_tags   = ["rest-proxy-${var.global_prefix}"]
}

resource "google_compute_firewall" "ksql_server" {
  count = var.instance_count["ksql_server"] >= 1 ? 1 : 0
  name    = "ksql-server-${var.global_prefix}"
  network = google_compute_network.default.name
  allow {
    protocol = "tcp"
    ports    = ["22", "8088"]
  }
  source_ranges = ["0.0.0.0/0"]
  target_tags   = ["ksql-server-${var.global_prefix}"]
}
