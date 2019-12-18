#
# Copyright 2019 Confluent Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM confluentinc/cp-base

ARG COMMIT_ID=unknown
LABEL io.confluent.docker.git.id=$COMMIT_ID
ARG BUILD_NUMBER=-1
LABEL io.confluent.docker.build.number=$BUILD_NUMBER

MAINTAINER partner-support@confluent.io
LABEL io.confluent.docker=true

# allow arg override of required env params
ARG KAFKA_ZOOKEEPER_CONNECT
ENV KAFKA_ZOOKEEPER_CONNECT=${KAFKA_ZOOKEEPER_CONNECT}
ARG KAFKA_ADVERTISED_LISTENERS
ENV KAFKA_ADVERTISED_LISTENERS=${KAFKA_ADVERTISED_LISTENERS}

ENV COMPONENT=kafka

# primary
EXPOSE 9092

RUN apt-get update

RUN rm -rf /var/lib/apt/lists/* \
    && echo "===> Adding confluent repository...${CONFLUENT_PACKAGES_REPO}/deb/${CONFLUENT_MAJOR_VERSION}.${CONFLUENT_MINOR_VERSION}" \
    && if [ "x$ALLOW_UNSIGNED" = "xtrue" ]; then echo "APT::Get::AllowUnauthenticated \"true\";" > /etc/apt/apt.conf.d/allow_unauthenticated; else curl -L https://s3-us-west-2.amazonaws.com/jenkins-confluent-packages/master/1170/deb/5.4/archive.key | apt-key add - ; fi \
    && echo "deb [arch=amd64] https://s3-us-west-2.amazonaws.com/jenkins-confluent-packages/master/1327/deb/5.4 stable main" >> /etc/apt/sources.list

RUN echo "====> installing the latest Confluent Server" \
    && apt-get -y update && apt-get -y install confluent-server confluent-security

# RUN echo "====> installing kafkacat" \
# && apt-get -y install kafkacat

RUN echo "====> installing iperf" \
    && apt-get -y install iperf

RUN echo "===> installing ${COMPONENT}..." \
    && apt-get update && apt-get install -y confluent-server \
    && echo "===> installing confluent-rebalancer ..." \
    && apt-get update && apt-get install -y \
    confluent-rebalancer \
    && echo "===> clean up ..."  \
    && apt-get clean && rm -rf /tmp/* /var/lib/apt/lists/* \
    \
    && echo "===> Setting up ${COMPONENT} dirs..." \
    && mkdir -p /var/lib/${COMPONENT}/data /etc/${COMPONENT}/secrets\
    && chmod -R ag+w /etc/${COMPONENT} /var/lib/${COMPONENT}/data /etc/${COMPONENT}/secrets \
    && chown -R root:root /var/log/kafka /var/log/confluent /var/lib/kafka /var/lib/zookeeper


VOLUME ["/var/lib/${COMPONENT}/data", "/etc/${COMPONENT}/secrets"]

COPY include/etc/confluent/docker /etc/confluent/docker

CMD ["/etc/confluent/docker/run"]
