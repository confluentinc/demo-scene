#!/bin/bash

build-docs() {
  cd /home/dc01/.workshop/docker/asciidoc
  asciidoctor index.adoc -o index.html -a stylesheet=stylesheet.css -a externalip=${ext_ip} -a dc=dc01 -a imagesdir=./images

  # Inject c&p functionality into rendered html file.
  sed -i -e '/<title>/r clipboard.html' index.html
}


build-docs
