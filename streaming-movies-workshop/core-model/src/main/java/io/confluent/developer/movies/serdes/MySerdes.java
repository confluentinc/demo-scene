package io.confluent.developer.movies.serdes;

import org.apache.avro.specific.SpecificRecord;

import java.util.Map;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

public class MySerdes {

  public static <T extends SpecificRecord> SpecificAvroSerde<T> getSpecificAvroSerde(final Map<String, String> serdeConfig) {
    final SpecificAvroSerde<T> specificAvroSerde = new SpecificAvroSerde<>();
    specificAvroSerde.configure(serdeConfig, false);
    return specificAvroSerde;
  }
  
}
