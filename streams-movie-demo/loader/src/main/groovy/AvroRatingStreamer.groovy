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
    props.load(new FileInputStream(new File(args[0])))

    def bootstrapServer = props.get('bootstrap.servers')
    println "Streaming ratings to ${ bootstrapServer}"
    println "Schema Registry at ${props.get('schema.registry.url')}"
    props.put('key.serializer', 'org.apache.kafka.common.serialization.LongSerializer')
    props.put('value.serializer', 'io.confluent.kafka.serializers.KafkaAvroSerializer')
    props.put('schema.registry.url', props.get('schema.registry.url'))

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
        Thread.sleep(250);
      }
    }
    finally {
      producer.close()
    }
  }
}
