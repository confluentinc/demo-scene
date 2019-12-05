#
# Copyright 2018 Confluent Inc.
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

FROM confluentinc/cp-kafka-connect:5.2.1

ENV CONNECT_PLUGIN_PATH="/usr/share/java,/usr/share/confluent-hub-components"

RUN confluent-hub install --no-prompt confluentinc/kafka-connect-datagen:latest
RUN confluent-hub install --no-prompt confluentinc/kafka-connect-solace-source:latest
RUN confluent-hub install --no-prompt confluentinc/kafka-connect-solace-sink:latest
RUN wget -O jms.zip https://products.solace.com/download/JMS_API
RUN apt-get update && apt-get install -y zip 
RUN unzip jms.zip
RUN cp sol-jms-*/lib/sol-jms-*.jar /usr/share/confluent-hub-components/confluentinc-kafka-connect-solace-source/lib/
RUN cp sol-jms-*/lib/sol-jms-*.jar /usr/share/confluent-hub-components/confluentinc-kafka-connect-solace-sink/lib

