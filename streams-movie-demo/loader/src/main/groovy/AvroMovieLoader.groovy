import io.confluent.demo.Movie
import io.confluent.demo.Parser
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

// Nasty little hack to generate random ratings for fun movies
class AvroMovieLoader {

  static void main(args) {

    Properties props = new Properties()
    props.load(new FileInputStream(new File(args[0])))

    def bootstrapServer = System.getenv('KAFKA_BOOTSTRAP_SERVERS') ?: props.get('bootstrap.servers')
    def schemaRegistryServer = System.getenv('KAFKA_SCHEMA_REGISTRY_URL') ?: props.get('schema.registry.url')
    println "Streaming ratings to ${bootstrapServer}"
    println "Schema Registry at ${schemaRegistryServer}"
    println "Movies File at ${new File(props.get('movies.file')).absolutePath}"

    props.put('key.serializer', 'org.apache.kafka.common.serialization.LongSerializer')
    props.put('value.serializer', 'io.confluent.kafka.serializers.KafkaAvroSerializer')
    props.put('schema.registry.url', schemaRegistryServer)

    KafkaProducer producer = new KafkaProducer(props)

    try {
      long currentTime = System.currentTimeSeconds()
      println currentTime

      def movieFile = new File(props.get('movies.file'))
      movieFile.eachLine { line ->
        Movie movie = Parser.parseMovie(line)
        def pr = new ProducerRecord('movies', movie.movieId, movie)
        producer.send(pr)
      }
    }
    finally {
      producer.close()
    }
  }
}
