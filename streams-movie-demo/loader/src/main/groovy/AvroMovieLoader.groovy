import io.confluent.demo.Movie
import io.confluent.demo.Parser
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

// Nasty little hack to generate random ratings for fun movies
class AvroMovieLoader {

   static void main(args) {

      Properties props = new Properties()
      props.put('bootstrap.servers', args[1])
      props.put('key.serializer', 'org.apache.kafka.common.serialization.LongSerializer')
      props.put('value.serializer', 'io.confluent.kafka.serializers.KafkaAvroSerializer')
      props.put('schema.registry.url', 'http://localhost:8081')
      KafkaProducer producer = new KafkaProducer(props)

      try {
         long currentTime = System.currentTimeSeconds()
         println currentTime

         println args[1]
         def movieFile = new File(args[0])
         movieFile.eachLine { line ->
           Movie movie = Parser.parseMovie(line)
           def pr = new ProducerRecord('raw-movies', movie.movieId, movie)
           producer.send(pr)
         }
      }
      finally {
         producer.close()
      }
   }
}
