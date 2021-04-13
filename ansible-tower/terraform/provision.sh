#!/bin/bash
cd "$(dirname "$0")"

ssh-keygen -b 2048 -t rsa -f sshkey -q -N ""

terraform init

terraform apply --auto-approve
