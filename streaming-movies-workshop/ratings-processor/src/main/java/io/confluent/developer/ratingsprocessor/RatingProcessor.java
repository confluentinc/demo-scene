package io.confluent.developer.ratingsprocessor;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.ValueJoiner;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

import io.confluent.developer.CountAndSum;
import io.confluent.developer.Movie;
import io.confluent.developer.RatedMovie;
import io.confluent.developer.Rating;
import io.confluent.developer.movies.serdes.MySerdes;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.apache.kafka.streams.kstream.Grouped.with;

@Component
@RequiredArgsConstructor
@Slf4j
public class RatingProcessor {

  private final Map<String, String> serdeConfig;

  @Value("${my.topics.movies.name:movies}")
  String movieTopicName;

  @Value("${my.topics.ratings.name:ratings}")
  String ratingsTopicName;

  @Value("${my.topic.avg-ratings.name:average-ratings}")
  String avgRatingsTopicName;

  @Value("${my.topic.rated-movies.name:rated-movies}")
  String ratedMoviesTopicName;

  @Autowired
  public void process(StreamsBuilder streamsBuilder) {
    final KTable<Long, Movie> movieKTable = this.moviesTable(streamsBuilder,
                                                             movieTopicName,
                                                             MySerdes.getSpecificAvroSerde(serdeConfig));

    final KStream<Long, Rating> ratingKStream = ratingsStream(streamsBuilder,
                                                              ratingsTopicName,
                                                              MySerdes.getSpecificAvroSerde(serdeConfig));

    final KTable<Long, Double> ratingAverageTable =
        ratingAverageTable(ratingKStream, avgRatingsTopicName, MySerdes.getSpecificAvroSerde(serdeConfig));

    ratedMoviesTable(movieKTable, ratingAverageTable, ratedMoviesTopicName, MySerdes.getSpecificAvroSerde(serdeConfig));

  }


  /**
   * Creates a materialized view of Movies topic
   *
   * @return KTable of Movies
   */
  public KTable<Long, Movie> moviesTable(StreamsBuilder builder,
                                         String moviesTopicName,
                                         SpecificAvroSerde<Movie> movieSerde) {

    // Movies table
    return builder.table(moviesTopicName,
                         Consumed.with(Serdes.Long(), movieSerde),
                         Materialized.as(moviesTopicName + "-store"));
  }

  public KStream<Long, Rating> ratingsStream(StreamsBuilder builder,
                                             String ratingsTopicName,
                                             SpecificAvroSerde<Rating> ratingsSerde) {
    return builder.stream(ratingsTopicName, Consumed.with(Serdes.Long(), ratingsSerde));

  }

  public KTable<Long, Double> ratingAverageTable(KStream<Long, Rating> ratings,
                                                 String avgRatingsTopicName,
                                                 SpecificAvroSerde<CountAndSum> countAndSumSerde) {

    // Grouping Ratings
    KGroupedStream<Long, Double> ratingsById = ratings
        .map((key, rating) -> new KeyValue<>(rating.getMovieId(), rating.getRating()))
        .groupByKey(with(Serdes.Long(), Serdes.Double()));

    final KTable<Long, Double> ratingAverage =
        ratingsById.aggregate(() -> new CountAndSum(0L, 0.0),
                              (key, value, aggregate) -> {
                                aggregate.setCount(aggregate.getCount() + 1);
                                aggregate.setSum(aggregate.getSum() + value);
                                return aggregate;
                              },
                              Materialized.with(Serdes.Long(), countAndSumSerde))
            .mapValues(value -> value.getSum() / value.getCount(),
                       Materialized.<Long, Double, KeyValueStore<Bytes, byte[]>>as("average-ratings")
                           .withKeySerde(Serdes.Long())
                           .withValueSerde(Serdes.Double()));

    // persist the result in topic
    ratingAverage.toStream().to(avgRatingsTopicName, Produced.with(Serdes.Long(), Serdes.Double()));
    return ratingAverage;
  }


  public KTable<Long, RatedMovie> ratedMoviesTable(KTable<Long, Movie> movies,
                                                   KTable<Long, Double> ratingAverage,
                                                   String ratedMovieTopic,
                                                   SpecificAvroSerde<RatedMovie> ratedMovieSerde) {

    ValueJoiner<Double, Movie, RatedMovie> joiner = (avg, movie) -> new RatedMovie(movie.getMovieId(),
                                                                                   movie.getTitle(),
                                                                                   movie.getReleaseYear(),
                                                                                   avg);
    KTable<Long, RatedMovie>
        ratedMovies =
        ratingAverage
            .join(movies, joiner, Materialized.<Long, RatedMovie, KeyValueStore<Bytes, byte[]>>as("rated-movies-store")
                .withValueSerde(ratedMovieSerde)
                .withKeySerde(Serdes.Long()));

    ratedMovies.toStream().to(ratedMovieTopic, Produced.with(Serdes.Long(), ratedMovieSerde));
    return ratedMovies;
  }
}
