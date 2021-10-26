package io.confluent.developer.adventure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PickupParser {
  private static Logger logger = LoggerFactory.getLogger(PickupParser.class);

  static InventoryCommandValue parse(String command) {
    String[] commandParts = command.split(" ");
    logger.info("Handing inventory request: {} {}", command, commandParts);
    if (commandParts.length == 2) {
      switch (commandParts[0]) {
      case "PICKUP":
        return new InventoryCommandValue("PICKUP", commandParts[1]);
      case "GET":
        // Alias.
        return new InventoryCommandValue("PICKUP", commandParts[1]);
      case "USE":
        return new InventoryCommandValue("USE", commandParts[1]);
      }
    }

    return null;
  }
}