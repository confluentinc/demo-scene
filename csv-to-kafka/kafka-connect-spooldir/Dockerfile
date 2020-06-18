FROM confluentinc/cp-kafka-connect-base:5.5.0
ENV CONNECT_PLUGIN_PATH="/usr/share/java,/usr/share/confluent-hub-components"
RUN confluent-hub install --no-prompt jcustenborder/kafka-connect-spooldir:2.0.43
