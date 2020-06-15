variable "zone" {
  type = string
  default = "europe-west4-b"
}

variable "project_name" {
  type = string
  default = "distributed-systems-279515"
}



provider "google" {
  project     = var.project_name
  region      = "europe-west4"
  zone        = var.zone
  credentials = file("account.json")
}

resource "google_container_registry" "registry" {
  location = "EU"
}

resource "google_container_cluster" "primary" {
  name     = "distributed-systems-demo-cluster"
  location = var.zone

  # We can't create a cluster with no node pool defined, but we want to only use
  # separately managed node pools. So we create the smallest possible default
  # node pool and immediately delete it.
  remove_default_node_pool = true
  initial_node_count       = 1

  master_auth {
    username = ""
    password = ""

    client_certificate_config {
      issue_client_certificate = false
    }
  }
}

resource "google_container_node_pool" "primary_nodes" {
  name       = "dis-sys-pool-1"
  location   = var.zone
  cluster    = google_container_cluster.primary.name
  node_count = 2

  node_config {
    preemptible  = false
    machine_type = "n1-standard-1"
    disk_size_gb = 10

    metadata = {
      disable-legacy-endpoints = "true"
    }

    oauth_scopes = [
      "https://www.googleapis.com/auth/logging.write",
      "https://www.googleapis.com/auth/monitoring",
    ]
  }
}
