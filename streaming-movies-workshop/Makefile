THIS_MKFILE_PATH := $(abspath $(lastword $(MAKEFILE_LIST)))
THIS_MKFILE_DIR := $(dir $(THIS_MKFILE_PATH))

include $(THIS_MKFILE_DIR)scripts/common/Makefile

GCP_PROJECT_ID ?= $(shell gcloud config list --format 'value(core.project)')
GKE_BASE_MACHINE_TYPE ?= n1-highmem-2

CLUSTER_NAME=livestreams-workshop
CLUSTER_ZONE=us-east1-c

gke-check-dependencies: check-dependencies
	@$(call check-var-defined,GCP_PROJECT_ID)
	@$(call check-dependency,gcloud)
	@$(call echo_pass,gke-base dependencies verified)

create-gke-cluster: gke-check-dependencies
	@$(call print-header,"Creating a new cluster Creating GKE")
	@$(call print-prompt)
	gcloud --quiet container --project $(GCP_PROJECT_ID) clusters create ${CLUSTER_NAME} --num-nodes 2 --machine-type $(GKE_BASE_MACHINE_TYPE) --zone ${CLUSTER_ZONE}

destroy-gke-cluster: gke-check-dependencies 
	@$(call print-header, "Delete GKE cluster")
	@$(call print-prompt)
	gcloud --quiet container --project $(GCP_PROJECT_ID) clusters delete ${CLUSTER_NAME} --zone ${CLUSTER_ZONE} 
	@$(call echo_stdout_footer_pass,GKE Cluster Deleted)

# TODO: 
# - gcp cluster scale gcloud container clusters resize livestreams-cluster --size=3 --zone us-east1-c

create-local-k3d-cluster:
	@$(call print-header,"Creating new k3d cluster")
	@$(call print-prompt)
	k3d cluster create ${CLUSTER_NAME} --servers 4 --volume $(PWD)/.data:/var/lib/host --wait

destroy-local-k3d-cluster:
	@$(call print-header,"Deleting k3d cluster...")
	@$(call print-prompt)
	-(k3d cluster list | grep ${CLUSTER_NAME}) && k3d cluster delete ${CLUSTER_NAME}

create-local-minikube-cluster:
	@$(call print-header,"Creating new minicube cluster")
	@$(call print-prompt)
	minikube start --cpus 4 --memory 6096

destroy-local-minikube-cluster:
	@$(call print-header,"Deleting minikube cluster...")
	@$(call print-prompt)
	minikube delete

ccloud:
	@echo "Installing ccloud"
	@curl -L -s --http1.1 https://cnfl.io/ccloud-cli | sh -s -- -b .
	@sudo install -m 755 ccloud /usr/local/bin/ccloud
	@rm -f ccloud
	@$(caller echo_stdout_footer_pass, "ccloud cli installed")

install-deps: ccloud
	@brew bundle
	@$(caller echo_stdout_footer_pass, "dependencies installed")

create-ccloud-cluster:
	@$(call print-header,"☁️ Creating ccloud Cluster...")
	@$(call print-prompt)
	./scripts/ccloud/ccloud_stack_create.sh

destroy-ccloud-cluster:
	@$(call print-header,"🧨 Destroying ccloud Cluster...")
	@$(call print-prompt)
	./scripts/ccloud/ccloud_stack_destroy.sh ${THIS_MKFILE_DIR}$(filter-out $@,$(MAKECMDGOALS))

