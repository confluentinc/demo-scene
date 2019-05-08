package io.confluent.demo.iq.json;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

import io.confluent.demo.Movie;

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
