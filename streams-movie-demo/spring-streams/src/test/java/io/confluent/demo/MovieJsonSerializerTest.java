package io.confluent.demo;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.demo.iq.KeyValueBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

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
        movie.setGenres(Arrays.asList("FDS"));
        movie.setActors(Arrays.asList("FDS"));
        movie.setComposers(Arrays.asList("FDS"));
        movie.setProductionCompanies(Arrays.asList("FDS"));
        movie.setCinematographer("FDS");
        KeyValueBean<String, Movie> fsd = new KeyValueBean<String, Movie>("test_key", movie);

//        String s = objectMapper.writeValueAsString(movie);
        String s = objectMapper.writeValueAsString(fsd);
        System.out.println("s = " + s);
    }
}
