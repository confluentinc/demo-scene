#!/bin/bash
cd "$(dirname "$0")"

terraform destroy --auto-approve

rm -rf id_rsa*
