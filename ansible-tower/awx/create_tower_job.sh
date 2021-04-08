#!/bin/bash
cd "$(dirname "$0")"

echo "________Build AWX CLI Docker Image________"
docker build . -t localhost/awx:latest

shopt -s expand_aliases
alias awx="docker run --network host --rm -it -v "${HOME}/.ssh:${HOME}/.ssh" --env TOWER_HOST=http://localhost:8052 --env TOWER_USERNAME=admin --env TOWER_PASSWORD=password localhost/awx:latest awx"

echo "________Create Default Organization________"
awx organizations create --name Default

echo "________Create cp-ansible project________"
awx projects create --wait \
    --organization Default --name='CP-Ansible' \
    --scm_type git --scm_branch='6.1.1-post' \
    --scm_url 'https://github.com/confluentinc/cp-ansible'

echo "________Create inventory project________"
awx projects create --wait \
    --organization Default --name='AWS Infrastructure' \
    --scm_type git --scm_branch='tower-demo' \
    --scm_url $REPO_URL

echo "________Create Inventory________"
awx inventory create \
    --organization Default --name='AWS Infrastructure'

echo "________Create Inventory Source from Inventory Project________"
awx inventory_sources create \
    --name='AWS Infrastructure' \
    --inventory='AWS Infrastructure' \
    --source_project='AWS Infrastructure' \
    --source scm \
    --source_path='ansible-tower/terraform/hosts.yml' \
    --update_on_project_update true

echo "________Create Machine Credential from SSH Key________"
awx credentials create --credential_type 'Machine' \
    --name 'AWS Key' --organization Default \
    --inputs '{"username": "centos", "ssh_key_data": "@'${HOME}'/.ssh/id_rsa"}'

echo "________Create Deployment Job________"
awx job_templates create \
    --name='Deploy on AWS' --project 'CP-Ansible' \
    --playbook all.yml --inventory 'AWS Infrastructure' \
    --credentials 'AWS Key'

echo "________Associate Machine Credential to Job________"
awx job_template associate \
    --credential 'AWS Key' 'Deploy on AWS'

unalias awx
