package io.confluent.developer.movies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.confluent.demo.CountAndSum;
import io.confluent.demo.Movie;
import io.confluent.demo.RatedMovie;
import io.confluent.demo.Rating;
import lombok.RequiredArgsConstructor;

import static io.confluent.demo.Parser.parseArray;

@Service
@RequiredArgsConstructor
public class RatingService {

  @Autowired
  private final JdbcTemplate jdbcTemplate;

  @Autowired
  private final KafkaTemplate<Long, RatedMovie> kafkaProducer;

  private static ConcurrentHashMap<Long, CountAndSum> countAndSumStorage = new ConcurrentHashMap<>();

  @KafkaListener(topics = "ratings", groupId = "rating_averager")
  private void ratingListener(Rating rating) {
    final Long movieId = rating.getMovieId();

    countAndSumStorage.compute(movieId,
                               (aLong, prevCountAndSum) -> {
                                 if (prevCountAndSum != null) {
                                   return new CountAndSum(prevCountAndSum.getCount() + 1,
                                                   prevCountAndSum.getSum() + rating.getRating());
                                 }
                                 return new CountAndSum(1L, rating.getRating());
                               });

    final Movie movie = getMovieFromDb(movieId);
    if (movie != null) {
      final CountAndSum countAndSum = countAndSumStorage.get(movieId);
      final RatedMovie ratedMovie = new RatedMovie(movie.getMovieId(), movie.getTitle(), movie.getReleaseYear(),
                                                   countAndSum.getSum() / countAndSum.getCount());

      System.out.println(ratedMovie);
      kafkaProducer.send("rated-movies", movieId, ratedMovie);

    } else {
      System.out.println("can't find movie with id: " + movieId);
    }
  }

  public Movie getMovieFromDb(Long movieId) {
    List<Movie> movies = jdbcTemplate.query("select movie_id,"
                                            + "title,"
                                            + "release_year,"
                                            + "country,"
                                            + "genres,"
                                            + "actors,"
                                            + "directors,"
                                            + "composers,"
                                            + "screenwriters,"
                                            + "cinematographer,"
                                            + "production_companies "
                                            + "from movies "
                                            + "where movie_id = ?",
                                            new Object[]{movieId},
                                            (rs, rowNum) -> Movie.newBuilder()
                                                .setMovieId(rs.getLong("movie_id"))
                                                .setTitle(rs.getString("title"))
                                                .setReleaseYear(rs.getInt("release_year"))
                                                .setCountry(rs.getString("country"))
                                                .setGenres(parseArray(rs.getString("genres")))
                                                .setActors(parseArray(rs.getString("actors")))
                                                .setDirectors(parseArray(rs.getString("directors")))
                                                .setComposers(parseArray(rs.getString("composers")))
                                                .setScreenwriters(parseArray(rs.getString("screenwriters")))
                                                .setCinematographer(rs.getString("cinematographer"))
                                                .setProductionCompanies(
                                                    parseArray(rs.getString("production_companies")))
                                                .build());
    return movies.get(0);
  }
}
