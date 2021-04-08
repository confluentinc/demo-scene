#!/bin/bash
cd "$(dirname "$0")"

shopt -s expand_aliases
alias tf="docker run -it --rm -v '$(pwd):$(pwd)' -w '$(pwd)' hashicorp/terraform:light"

type -a tf

tf destroy --auto-approve

unalias tf
