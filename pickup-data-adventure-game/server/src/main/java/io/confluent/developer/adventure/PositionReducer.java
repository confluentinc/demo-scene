package io.confluent.developer.adventure;

import org.apache.kafka.streams.kstream.Reducer;

public class PositionReducer implements Reducer<Position> {
  public Position apply(Position p1, Position p2) {
    return new Position(p1.getX() + p2.getX(), p1.getY() + p2.getY());
  }
}