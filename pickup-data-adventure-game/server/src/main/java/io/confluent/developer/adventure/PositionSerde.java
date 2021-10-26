package io.confluent.developer.adventure;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

public class PositionSerde {
  static final Serde<Position> Serde;

  static {
    Map<String, Object> serdeProps = new HashMap<>();
    final Serializer<Position> positionSerializer = new JsonPOJOSerializer<>();
    serdeProps.put("JsonPOJOClass", Position.class);
    positionSerializer.configure(serdeProps, false);

    final Deserializer<Position> positionDeserializer = new JsonPOJODeserializer<>();
    serdeProps.put("JsonPOJOClass", Position.class);
    positionDeserializer.configure(serdeProps, false);

    Serde = Serdes.serdeFrom(positionSerializer, positionDeserializer);
  }
}
