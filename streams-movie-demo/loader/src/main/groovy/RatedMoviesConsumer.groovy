import groovy.transform.CompileStatic
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.LongDeserializer
import org.apache.kafka.common.serialization.StringDeserializer

import static java.time.Duration.ofMillis
import static org.apache.kafka.clients.consumer.ConsumerConfig.*

@CompileStatic
class RatedMoviesConsumer {

  static final def topicName = 'rated-movies'

  static void main(args) {

    def p = new Properties()
    p[BOOTSTRAP_SERVERS_CONFIG] = 'localhost:9092'
    p[GROUP_ID_CONFIG] = 'rated-movies-groovy-consumer'
    p[KEY_DESERIALIZER_CLASS_CONFIG] = LongDeserializer.name
    p[VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer.name

    def consumer = new KafkaConsumer(p)
    consumer.subscribe([topicName] as List<String>)

    while (true) {
      def records = consumer.poll(ofMillis(100))
      for (record in records) {
        println record.value()
      }
    }
  }
}
