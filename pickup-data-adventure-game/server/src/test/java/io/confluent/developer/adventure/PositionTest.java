package io.confluent.developer.adventure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;

import org.apache.kafka.common.serialization.Serde;
import org.junit.jupiter.api.Test;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.arbitraries.IntegerArbitrary;

class PositionTest {
  String testTopic = "test";

  @Test
  void serialisationExamples() {
    Position p1 = new Position(-3, 1);
    String expectedEncoding = "{\"X\":-3,\"Y\":1}";

    Serde<Position> positionSerde = PositionSerde.Serde;

    byte[] encoded = positionSerde.serializer().serialize(testTopic, p1);
    String encodedString = new String(encoded, StandardCharsets.UTF_8);
    assertEquals(expectedEncoding, encodedString, "Position serializes to the expected format.");

    Position p2 = positionSerde.deserializer().deserialize(testTopic, expectedEncoding.getBytes());
    assertEquals(p1, p2, "Position deserializes from the expected format.");
  }

  @Property
  public void serialisationRoundtrip(@ForAll("arbitraryPosition") Position p1) {
    Serde<Position> positionSerde = PositionSerde.Serde;
    byte[] encoded = positionSerde.serializer().serialize(testTopic, p1);
    Position p2 = positionSerde.deserializer().deserialize(testTopic, encoded);
    assertEquals(p1, p2);
  }

  @Provide
  Arbitrary<Position> arbitraryPosition() {
    IntegerArbitrary xs = Arbitraries.integers().between(-100, 100);
    IntegerArbitrary ys = Arbitraries.integers().between(-100, 100);

    return Combinators.combine(xs, ys).as((x, y) -> new Position(x, y));
  }
}
