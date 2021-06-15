FROM ubuntu:bionic
MAINTAINER Gianluca Natali gnatali@confluent.io

ENV PATH /opt:$PATH
 
RUN apt-get -y update
RUN apt-get -y upgrade
RUN apt-get install -y build-essential
RUN apt-get install python3 -y
RUN apt-get install python3-yaml -y
RUN update-alternatives --install /usr/bin/python python /usr/bin/python3 1
RUN apt-get install wget unzip -y
RUN apt-get install curl -y
RUN apt-get install jq -y
RUN apt-get install git -y
RUN apt-get install nano -y

RUN wget --quiet https://releases.hashicorp.com/terraform/1.0.0/terraform_1.0.0_linux_amd64.zip \
  && unzip terraform_1.0.0_linux_amd64.zip \
  && mv terraform /usr/bin \
  && rm terraform_1.0.0_linux_amd64.zip

RUN curl -L --http1.1 https://cnfl.io/ccloud-cli | sh -s -- -b /opt

RUN apt-get install nodejs npm -y
RUN npm install -g mongodb-realm-cli
