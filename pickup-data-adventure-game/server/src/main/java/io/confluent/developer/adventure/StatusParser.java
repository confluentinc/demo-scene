package io.confluent.developer.adventure;

public class StatusParser {
  static StatusCommandValue parse(String command) {
    switch (command) {
    case "INVENTORY":
      return new StatusCommandValue("INVENTORY");
    default:
      return null;
    }
  }
}