package io.confluent.developer.movies;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.confluent.developer.CountAndSum;
import io.confluent.developer.Movie;
import io.confluent.developer.RatedMovie;
import io.confluent.developer.Rating;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static io.confluent.developer.Parser.parseArray;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RatingService {

  private final JdbcTemplate jdbcTemplate;

  // in-memory store for intermediate ratings count and sum
  private static final ConcurrentHashMap<Long, CountAndSum> countAndSumStorage = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<Long, RatedMovie> ratedMovieStorage = new ConcurrentHashMap<>();

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
      ratedMovieStorage.put(movie.getMovieId(), ratedMovie);
    } else {
      log.warn("can't find movie with id: " + movieId);
    }
  }

  @RequestMapping(path = "/average", method = GET)
  public String movieAverage(@RequestParam Long movieId) {
    System.out.println("movieId="+movieId);
    System.out.println(ratedMovieStorage.get(movieId));
    return ratedMovieStorage.get(movieId).toString();
  }


  // getting movie by id from MySQL database
  private Movie getMovieFromDb(Long movieId) {
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
