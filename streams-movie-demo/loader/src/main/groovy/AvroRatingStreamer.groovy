import io.confluent.demo.Rating
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

import static io.confluent.demo.RatingUtil.generateRandomRating
import static io.confluent.demo.RatingUtil.ratingTargets

// Nasty little hack to generate random ratings for fun movies
class AvroRatingStreamer {

  static void main(args) {
    def stddev = 2

    Properties props = new Properties()
    props.put('bootstrap.servers', args[0])
    props.put('key.serializer', 'org.apache.kafka.common.serialization.LongSerializer')
    props.put('value.serializer', 'io.confluent.kafka.serializers.KafkaAvroSerializer')
    props.put('schema.registry.url', 'http://localhost:8081')
    KafkaProducer producer = new KafkaProducer(props)

    try {
      long currentTime = System.currentTimeSeconds()
      println currentTime
      long recordsProduced = 0
      while (true) {

        Rating rating = generateRandomRating(ratingTargets, stddev)

        //println "${System.currentTimeSeconds()}, ${currentTime}"
        if (System.currentTimeSeconds() > currentTime) {

          currentTime = System.currentTimeSeconds()
          println "RATINGS PRODUCED ${recordsProduced}"
        }
        def pr = new ProducerRecord('ratings', rating.movieId, rating)
        producer.send(pr)
        recordsProduced++
      }
    }
    finally {
      producer.close()
    }
  }
}
