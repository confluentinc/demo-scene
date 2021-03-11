#!/bin/bash

rm -rf deploy
mkdir -p deploy

mvn clean package
mv target/streaming-pacman-1.0.jar deploy/streaming-pacman-1.0.jar
