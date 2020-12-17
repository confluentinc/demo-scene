package io.confluent.developer.moviesgenerator;

import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.stream.Stream;

import io.confluent.developer.Movie;
import io.confluent.developer.Rating;
import io.confluent.developer.movies.util.Parser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class Producer {

  public static final String MOVIES_TOPIC = "movies";
  public static final String RATINGS_TOPIC = "ratings";
  private final KafkaTemplate<Long, SpecificRecordBase> kafkaTemplate;

  @Value(value = "classpath:movies.dat")
  private Resource moviesFile;

  @EventListener(ApplicationStartedEvent.class)
  public void process() throws InterruptedException {
    try (Stream<String> stream = Files.lines(Paths.get(moviesFile.getURI()))) {
      stream.forEach(s -> {
        Movie movie = Parser.parseMovie(s);
        log.info("sending movie {} with id {} to {} topic", movie.getTitle(), movie.getMovieId(), MOVIES_TOPIC);
        kafkaTemplate.send(MOVIES_TOPIC, movie.getMovieId(), movie);
      });
    } catch (IOException e) {
      e.printStackTrace();
    }

    Random ran = new Random();
    while (true) {
      int movieId = ran.nextInt(920) + 1;
      int rating = 5 + ran.nextInt(6);
      Rating rat = new Rating((long) movieId, (double) rating);
      log.info(rat.toString());
      Thread.sleep(100);

      this.kafkaTemplate.send(RATINGS_TOPIC, rat.getMovieId(), rat);
    }
  }
}