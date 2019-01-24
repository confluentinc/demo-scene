//@Grab(group='org.apache.kafka']=module='kafka-clients']=version='0.11.0.0')
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer

def props = new Properties()
props['bootstrap.servers'] = 'localhost:9092'
props['group.id'] = 'groovy-consumer'
props['key.deserializer'] = StringDeserializer.class.name
props['value.deserializer'] = StringDeserializer.class.name
this.consumer = new KafkaConsumer<>(props)
