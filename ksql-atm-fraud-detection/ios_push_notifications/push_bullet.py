#!/bin/python
# Taken from https://raw.githubusercontent.com/saubury/stream-smarts/master/scripts/python/kafka_notifier.py
# Heavily borrowing from https://www.confluent.io/blog/real-time-syslog-processing-with-apache-kafka-and-ksql-part-2-event-driven-alerting-with-slack/
# rmoff / October 9, 2018
#
# Install pushbullet library with
#  $ pip install --user pushbullet.py

from confluent_kafka import Consumer, KafkaError, TopicPartition
from pushbullet import Pushbullet
import json
import requests


# API keys held in a non-commited file
import credentials

# Subscribe to ATM_POSSIBLE_FRAUD_ENRICHED topic
settings = {
    'bootstrap.servers': 'localhost:9092',
    'group.id': 'python_pushbullet',
    'default.topic.config': {'auto.offset.reset': 'largest'}
}
c = Consumer(settings)
tp = TopicPartition('ATM_POSSIBLE_FRAUD_ENRICHED',1,-1)

print('seek: Seeking to %s' % tp)
c.seek(tp)
c.subscribe(['ATM_POSSIBLE_FRAUD_ENRICHED'])

# Connect to pushbullet service
pb = Pushbullet(credentials.login['pushbullet_api_token'])

# Poll for messages; and extract JSON and call pushbullet for any messages
while True:
    msg = c.poll()
    if msg.error():
        if msg.error().code() == KafkaError._PARTITION_EOF:
            continue
        else:
            print(msg.error())
            break

    app_json_msg = json.loads(msg.value().decode('utf-8'))

    title='Fraud alert from KSQL!' 
    body='Customer: %s\nATM1: %s / ATM2: %s' % (app_json_msg['CUSTOMER_NAME'], app_json_msg['T1_ATM'], app_json_msg['T2_ATM'])

    # Send a push notification to phone via push-bullet
    push = pb.push_note(title + '\nCustomer: %s / ATM1: %s / ATM2: %s' % (app_json_msg['CUSTOMER_NAME'], app_json_msg['T1_ATM'], app_json_msg['T2_ATM']) ,body)
    print('%s\n%s--\n' % (title , body))

c.close()
