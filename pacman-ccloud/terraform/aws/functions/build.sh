#!/bin/bash

rm -rf deploy
mkdir -p deploy

mvn clean
mvn compile
mvn package
mv target/aws-functions-1.0.jar deploy/aws-functions-1.0.jar
