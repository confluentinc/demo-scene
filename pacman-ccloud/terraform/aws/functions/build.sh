#!/bin/bash

rm -rf deploy
mkdir -p deploy

mvn clean
mvn compile
mvn package
mv target/aws-event-handler-1.0.jar deploy/aws-event-handler-1.0.jar
