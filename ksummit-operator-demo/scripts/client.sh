#!/bin/bash

set -e

platform=$1
[ $# -ne 1 ] && { echo "Usage: $0 platform name gcp/aws"; exit 1; }
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

## create keystore
echo "**************CREATE KEYSTORE FOR CLIENT..."
./createKeystore.sh "$DIR/../${platform}/certs/client/client.pem" "$DIR/../${platform}/certs/client/client-key.pem" "${platform}"

## create trustore
echo "************CREATE TRUSTORE FOR CLIENT..."
./createTrustStore.sh "$DIR/../${platform}/certs/ca/cacerts.pem" "$platform"


