package io.confluent.developer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import io.confluent.developer.avro.Movie;
import io.confluent.developer.avro.RawMovie;

import static java.util.Collections.singletonList;

public class TransformationEngine implements Runnable {

  private String inputTopic;
  private String outputTopic;
  private final AtomicBoolean closed = new AtomicBoolean(false);
  private KafkaConsumer<String, RawMovie> rawConsumer;
  private KafkaProducer<String, Movie> producer;

  public TransformationEngine(String inputTopic, String outputTopic,
                              KafkaConsumer<String, RawMovie> rawConsumer,
                              KafkaProducer<String, Movie> producer) {

    this.inputTopic = inputTopic;
    this.outputTopic = outputTopic;
    this.rawConsumer = rawConsumer;
    this.producer = producer;

  }

  public void run() {

    try {

      rawConsumer.subscribe(singletonList(inputTopic));

      while (!closed.get()) {

        ConsumerRecords<String, RawMovie> records = rawConsumer.poll(Duration.ofSeconds(1));
        for (ConsumerRecord<String, RawMovie> record : records) {

          Movie movie = convertRawMovie(record.value());
          ProducerRecord<String, Movie> transformedRecord =
              new ProducerRecord<String, Movie>(outputTopic, movie);

          producer.send(transformedRecord);

        }

      }

    } catch (WakeupException wue) {

        if (!closed.get()) {
            throw wue;
        }

    } finally {

      rawConsumer.close();
      producer.close();

    }

  }

  public void shutdown() {

    closed.set(true);
    rawConsumer.wakeup();

  }

  private Movie convertRawMovie(RawMovie rawMovie) {

    String[] titleParts = rawMovie.getTitle().split("::");
    String title = titleParts[0];
    int releaseYear = Integer.parseInt(titleParts[1]);

    return new Movie(rawMovie.getId(), title,
                     releaseYear, rawMovie.getGenre());

  }

}
