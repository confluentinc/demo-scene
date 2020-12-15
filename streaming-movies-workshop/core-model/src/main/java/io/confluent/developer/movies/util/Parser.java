package io.confluent.developer.movies.util;

import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import io.confluent.developer.Movie;
import io.confluent.developer.Rating;

public class Parser {

  private static List<String> parseArray(String text) {
    return Collections.list(new StringTokenizer(text, "|")).stream()
        .map(token -> (String) token)
        .collect(Collectors.toList());
  }

  public static Movie parseMovie(String text) {
    String[] tokens = text.split("\\:\\:");
    String id = tokens[0];
    String title = tokens[1];
    String releaseYear = tokens[2];
    String country = tokens[4];
    String genres = tokens[7];
    String actors = tokens[8];
    String directors = tokens[9];
    String composers = tokens[10];
    String screenwriters = tokens[11];
    String cinematographer = tokens[12];
    String productionCompanies = "";
    if (tokens.length > 13) {
      productionCompanies = tokens[13];
    }

    Movie movie = new Movie();
    movie.setMovieId(Long.parseLong(id));
    movie.setTitle(title);
    movie.setReleaseYear(Integer.parseInt(releaseYear));
    movie.setCountry(country);
    movie.setGenres(Parser.parseArray(genres));
    movie.setActors(Parser.parseArray(actors));
    movie.setDirectors(Parser.parseArray(directors));
    movie.setComposers(Parser.parseArray(composers));
    movie.setScreenwriters(Parser.parseArray(screenwriters));
    movie.setCinematographer(cinematographer);
    movie.setProductionCompanies(Parser.parseArray(productionCompanies));

    return movie;
  }

  // userid::movieid::rating
  public static Rating parseRating(String text) {
    String[] tokens = text.split("\\:\\:");

    String movieId = tokens[1];
    String userRating = tokens[2];

    Rating rating = new Rating();
    rating.setMovieId(Long.parseLong(movieId));
    rating.setRating(Double.parseDouble(userRating));

    return rating;
  }


}