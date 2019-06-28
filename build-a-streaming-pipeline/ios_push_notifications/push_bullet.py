#!/bin/python
# Taken from https://raw.githubusercontent.com/saubury/stream-smarts/master/scripts/python/kafka_notifier.py
# Heavily borrowing from https://www.confluent.io/blog/real-time-syslog-processing-with-apache-kafka-and-ksql-part-2-event-driven-alerting-with-slack/
# rmoff / October 9, 2018
#
# Install pushbullet library with
#  $ pip install --user pushbullet.py

from confluent_kafka import Consumer, KafkaError
from pushbullet import Pushbullet
import json
import requests
import time

# API keys held in a non-committed file
import credentials

# Subscribe to UNHAPPY_PLATINUM_CUSTOMERS topic
settings = {
    'bootstrap.servers': 'localhost:9092',
    'group.id': 'python_pushbullet',
    'default.topic.config': {'auto.offset.reset': 'largest'}
}
c = Consumer(settings)
# tp = TopicPartition('UNHAPPY_PLATINUM_CUSTOMERS',1,-1)

# print('seek: Seeking to %s' % tp)
# c.seek(tp)
c.subscribe(['UNHAPPY_PLATINUM_CUSTOMERS'])

# Connect to pushbullet service
pb = Pushbullet(credentials.login['pushbullet_api_token'])
try:
    while True:
        msg = c.poll(0.1)
        # Artificially slow down processing for demo purposes
        # A deluge of push notifications is just confusing. 
        time.sleep(5)
        if msg is None:
            continue
        elif not msg.error():
            print('Received message: {0}'.format(msg.value()))
            if msg.value() is None:
                continue
            try:
                app_msg = json.loads(msg.value().decode())
            except:
                app_msg = json.loads(msg.value())
            try:
                email=app_msg['EMAIL']
                message=app_msg['MESSAGE']
                title='Unhappy customer! %s' % (email)
                text=('`%s` just left a bad review :disappointed:\n> %s\n\n_Please contact them immediately and see if we can fix the issue *right here, right now*_' % (email, message))
                print('\nSending message %s/%s' % (title,text))
            except:
                print('Failed to get channel/text from message')
                text=msg.value()
                title='Notification'
            try:
                push = pb.push_note(title,text)
            except Exception as e:
                print(type(e))
                print(dir(e))
        elif msg.error().code() == KafkaError._PARTITION_EOF:
            print('End of partition reached {0}/{1}'
                  .format(msg.topic(), msg.partition()))
        else:
            print('Error occured: {0}'.format(msg.error().str()))

except Exception as e:
    print(type(e))
    print(dir(e))

finally:
    c.close()
