package io.confluent.demo.iq.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

import io.confluent.demo.Movie;

import javax.annotation.PostConstruct;

@JsonComponent
public class MovieJsonSerializer extends JsonSerializer<Movie> {

  @Override
  public void serialize(Movie value, JsonGenerator generator, SerializerProvider provider) throws IOException {
    System.out.println("---------------------- movie json");
    generator.writeStartObject();
    generator.writeStringField("movie", value.toString());
    generator.writeEndObject();
  }
}
