// use for standalone script
// in other cases use Gradle to manage dependency
//@Grab(group = 'org.apache.kafka', module = 'kafka-clients', version = '2.0.0')
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.common.serialization.LongSerializer
import org.apache.kafka.clients.producer.ProducerRecord

def p = new Properties()
p['bootstrap.servers'] = 'localhost:9092'
p['key.serializer'] = LongSerializer.class.name
p['value.serializer'] = StringSerializer.class.name

def producer = new KafkaProducer(p)

new File('data/movies.dat').eachLine { line ->
  def fields = line.split('::')
  producer.send(new ProducerRecord('movies', new Long(fields[0]), fields[1]))
}

new File('data/ratings.dat').eachLine { line ->
  def fields = line.split('::')
  producer.send(new ProducerRecord('ratings', new Long(fields[1]), fields[2]))
}
