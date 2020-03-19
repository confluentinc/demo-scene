import io.confluent.demo.Parser
import io.confluent.demo.Rating
import io.confluent.devx.kafka.config.ConfigLoader
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.LongSerializer
import org.apache.kafka.common.serialization.StringSerializer

import static io.confluent.demo.RatingUtil.generateRandomRating
import static io.confluent.demo.RatingUtil.ratingTargets
import static java.lang.System.getenv

// Nasty little hack to generate random ratings for fun movies
class RawRatingStreamer {

  static void main(String[] args) {
    def stddev = 2

    def bootstrapServer = ""
    if (bootstrapServer?.trim()) {
      bootstrapServer = getenv('KAFKA_BOOTSTRAP_SERVERS')
    }
    if (args.length != 0) {
      bootstrapServer = args?.getAt(0)
    }
    Properties props = ConfigLoader.loadConfig(args?.getAt(0))
    //println "Streaming ratings to ${bootstrapServer}"
    //props.put('bootstrap.servers', bootstrapServer)
    props.put('key.serializer', LongSerializer.class.getName())
    props.put('value.serializer', StringSerializer.class.getName())

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
        def pr = new ProducerRecord('raw-ratings', rating.movieId, Parser.toRawRating(rating).toString())
        producer.send(pr)
        recordsProduced++
      }
    }
    finally {
      producer.close()
    }
  }

}
