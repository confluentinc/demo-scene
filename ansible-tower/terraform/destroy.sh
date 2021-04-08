#!/bin/bash
cd "$(dirname "$0")"

terraform destroy --auto-approve
