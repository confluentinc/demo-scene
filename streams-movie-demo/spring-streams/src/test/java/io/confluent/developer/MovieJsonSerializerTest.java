package io.confluent.developer;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.confluent.developer.iq.KeyValueBean;

import static java.util.Collections.singletonList;

@JsonTest
@RunWith(SpringRunner.class)
public class MovieJsonSerializerTest {

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  public void should_work() throws JsonProcessingException {
    Movie movie = new Movie();
    movie.setMovieId(11L);
    movie.setTitle("title");
    movie.setReleaseYear(1222);
    movie.setCountry("Fds");
    movie.setGenres(singletonList("FDS"));
    movie.setActors(singletonList("FDS"));
    movie.setComposers(singletonList("FDS"));
    movie.setProductionCompanies(singletonList("FDS"));
    movie.setCinematographer("FDS");
    KeyValueBean<String, Movie> fsd = new KeyValueBean<String, Movie>("test_key", movie);

//        String s = objectMapper.writeValueAsString(movie);
    String s = objectMapper.writeValueAsString(fsd);
    System.out.println("s = " + s);
  }
}
