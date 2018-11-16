import groovy.transform.CompileStatic
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer

import static java.time.Duration.ofMillis
import static java.util.Arrays.asList
import static org.apache.kafka.clients.consumer.ConsumerConfig.*

@CompileStatic
class RatedMoviesConsumer {
  public static final String topicName = "rated-movies"

  static void main(args) {

    Properties p = new Properties()
    p.put(BOOTSTRAP_SERVERS_CONFIG, 'localhost:9092')
    p.put(GROUP_ID_CONFIG, 'rated-movies-groovy-consumer')
    p.put(KEY_DESERIALIZER_CLASS_CONFIG, 'org.apache.kafka.common.serialization.LongDeserializer')
    p.put(VALUE_DESERIALIZER_CLASS_CONFIG, 'org.apache.kafka.common.serialization.StringDeserializer')

    KafkaConsumer consumer = new KafkaConsumer(p)
    consumer.subscribe(asList(topicName))

    while (true) {
      ConsumerRecords records = consumer.poll(ofMillis(100))
      for (it in records) {
        println it.value()
      }
    }
  }
}
