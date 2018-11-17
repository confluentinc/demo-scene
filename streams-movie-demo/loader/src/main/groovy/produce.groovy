//@Grab(group='org.apache.kafka', module='kafka-clients', version='0.11.0.0')
//import org.apache.kafka.clients.producer.KafkaProducer
//import org.apache.kafka.common.serialization.StringSerializer
//import org.apache.kafka.common.serialization.LongSerializer
//import org.apache.kafka.clients.producer.ProducerRecord
//
//
//Properties props = new Properties()
//props.put('bootstrap.servers', 'localhost:9092')
//props.put('key.serializer', LongSerializer.class.getName())
//props.put('value.serializer', StringSerializer.class.getName())
//def producer = new KafkaProducer(props)
//
//
//new File('data/movies.dat').eachLine { line ->
//  def fields = line.split('::')
//  producer.send(new ProducerRecord('movies', new Long(fields[0]), fields[1]))
//}
//
//new File('data/ratings.dat').eachLine { line ->
//  def fields = line.split('::')
//  producer.send(new ProducerRecord('ratings', new Long(fields[1]), fields[2]))
//}
