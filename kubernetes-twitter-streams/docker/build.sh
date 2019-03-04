#!/usr/bin/env bash

#TODO
declare -r GCR=gcr.io/cloud-private-dev
declare -r IMAGE_NAME="gamussa/kafka-connect-twitter"
declare -r IMAGE_TAG="0.2.32"

echo "Building image '$IMAGE_NAME:$IMAGE_TAG'"
docker build -t $IMAGE_NAME:$IMAGE_TAG .

#https://cloud.google.com/container-registry/docs/pushing-and-pulling
docker tag $IMAGE_NAME:$IMAGE_TAG $GCR/$IMAGE_NAME:$IMAGE_TAG
# docker push gcr.io/cloud-private-dev/gamussa/kafka-connect-twitter:0.2.32
