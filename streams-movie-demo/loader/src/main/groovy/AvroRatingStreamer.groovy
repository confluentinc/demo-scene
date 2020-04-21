import io.confluent.developer.Rating
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

import static io.confluent.developer.RatingUtil.*

// Nasty little hack to generate random ratings for fun movies
class AvroRatingStreamer {

  static void main(args) {
    def stddev = 2

    Properties props = new Properties()
    props.load(new FileInputStream(new File(args[0])))

    def bootstrapServer = System.getenv('KAFKA_BOOTSTRAP_SERVERS') ?: props.get('bootstrap.servers')
    def schemaRegistryServer = System.getenv('KAFKA_SCHEMA_REGISTRY_URL') ?: props.get('schema.registry.url')
    println "Streaming ratings to ${bootstrapServer}"
    println "Schema Registry at ${schemaRegistryServer}"
    props.put('key.serializer', 'org.apache.kafka.common.serialization.LongSerializer')
    props.put('value.serializer', 'io.confluent.kafka.serializers.KafkaAvroSerializer')
    props.put('schema.registry.url', schemaRegistryServer)

    KafkaProducer producer = new KafkaProducer(props)

    try {
      long currentTime = System.currentTimeSeconds()
      println currentTime
      long recordsProduced = 0
      while (true) {

        Rating rating = generateRandomRating(ratingTargets, userTargets, stddev)

        //println "${System.currentTimeSeconds()}, ${currentTime}"
        if (System.currentTimeSeconds() > currentTime) {

          currentTime = System.currentTimeSeconds()
          println "RATINGS PRODUCED ${recordsProduced}"
        }
        def pr = new ProducerRecord('ratings', rating.movieId, rating)
        producer.send(pr)
        recordsProduced++
        Thread.sleep(250)
      }
    }
    finally {
      producer.close()
    }
  }
}
