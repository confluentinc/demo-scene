import csv

from confluent_kafka import Producer
from confluent_kafka.serialization import SerializationContext, MessageField, StringSerializer
from confluent_kafka.schema_registry import SchemaRegistryClient
from confluent_kafka.schema_registry.avro import AvroSerializer


class Review(object):
    """
    Product Review Record

    Args:
        id (int): Review id

        product_id (str): Id of the product purchased

        user_id (str): User id

        timestamp(int): timestamp of purchase

        summary(str): Review summary
    """
    def __init__(self, id, product_id, user_id, timestamp, summary):
        self.id = id
        self.product_id = product_id
        self.user_id = user_id
        self.timestamp = timestamp
        self.summary = summary



def reading_to_dict(review, ctx):
    """
    Returns a dict representation of a Review instance for serialization.

    Args:
        review (Review): Review instance.

        ctx (SerializationContext): Metadata pertaining to the serialization
            operation.

    Returns:
        dict: Dict populated with product review attributes to be serialized.
    """
    return dict(id=review.id,
                product_id=review.product_id,
                user_id=review.user_id,
                timestamp=review.timestamp,
                summary=review.summary)


def delivery_report(err, msg):
    """
    Reports the failure or success of a message delivery.

    Args:
        err (KafkaError): The error that occurred on None on success.

        msg (Message): The message that was produced or failed.

    Note:
        In the delivery report callback the Message.key() and Message.value()
        will be the binary format as encoded by any configured Serializers and
        not the same object that was passed to produce().
        If you wish to pass the original object(s) for key and value to delivery
        report callback we recommend a bound callback or lambda where you pass
        the objects along.
        in this case, msg.key() will return the sensor device id, since, that is set
        as the key in the message.
    """
    if err is not None:
        print("Delivery failed for Review record {}: {}".format(msg.key(), err))
        return
    print('Review record with Id {} successfully produced to Topic:{} Partition: [{}] at offset {}'.format(
        msg.key(), msg.topic(), msg.partition(), msg.offset()))


def main():
    topic = 'product_reviews'
    schema = 'review.avsc'

    cc_config = {
        'bootstrap.servers': '<BOOTSTRAP SERVERS ENDPOINT>',
        'security.protocol': 'SASL_SSL',
        'sasl.mechanisms': 'PLAIN',
        'sasl.username': '<KAFKA API KEY>',
        'sasl.password': '<KAFKA API SECRET>'
    }

    sr_config = {
        'url': '<SR ENDPOINT URL>',
        'basic.auth.user.info': '<SR API KEY>:<SR API SECRET>'
    }

    with open(f"{schema}") as f:
        schema_str = f.read()

    schema_registry_conf = sr_config
    schema_registry_client = SchemaRegistryClient(schema_registry_conf)

    avro_serializer = AvroSerializer(schema_registry_client,
                                     schema_str,
                                     reading_to_dict)
    string_serializer = StringSerializer('utf_8')

    producer = Producer(cc_config)

    print("Producing review records to topic {}. ^C to exit.".format(topic))

    with open('reviews.csv', 'r') as f:
        next(f)
        reader = csv.reader(f, delimiter=',')
        for column in reader:
            review = Review(id=int(column[0]),
                            product_id=column[1],
                            user_id=column[2],
                            timestamp=int(column[3]),
                            summary=column[4])

            producer.produce(topic=topic,
                             key=string_serializer(str(review.product_id), SerializationContext(topic=topic, field=MessageField.KEY)),
                             value=avro_serializer(review, SerializationContext(topic, MessageField.VALUE)),
                             on_delivery=delivery_report)

    producer.poll(10000)
    producer.flush()


if __name__ == '__main__':
    main()
