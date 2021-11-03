package io.confluent.developer.adventure;

import static io.confluent.developer.adventure.Constants.COMMANDS_STREAM;
import static io.confluent.developer.adventure.Constants.INVENTORY_COMMAND_STREAM;
import static io.confluent.developer.adventure.Constants.MOVEMENT_COMMAND_STREAM;
import static io.confluent.developer.adventure.Constants.RESPONSES_STREAM;
import static io.confluent.developer.adventure.Constants.STATUS_COMMAND_STREAM;

import java.util.Map;
import java.util.UUID;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.BranchedKStream;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

public class InitialCommandProcessor {
  public static void buildStreams(Map<String, String> schemaRegistryProps, StreamsBuilder builder) {
    var commandValueSerde = new SpecificAvroSerde<CommandValue>();
    commandValueSerde.configure(schemaRegistryProps, false);
    var movementCommandValueSerde = new SpecificAvroSerde<MovementCommandValue>();
    movementCommandValueSerde.configure(schemaRegistryProps, false);
    var statusCommandValueSerde = new SpecificAvroSerde<StatusCommandValue>();
    statusCommandValueSerde.configure(schemaRegistryProps, false);
    var inventoryCommandValueSerde = new SpecificAvroSerde<InventoryCommandValue>();
    inventoryCommandValueSerde.configure(schemaRegistryProps, false);
    var responseValueSerde = new SpecificAvroSerde<ResponseValue>();
    responseValueSerde.configure(schemaRegistryProps, false);
    var producedResponse = Produced.with(Serdes.UUID(), responseValueSerde);

    BranchedKStream<UUID, CommandValue> commandBranches =
        builder.stream(COMMANDS_STREAM, Consumed.with(Serdes.UUID(), commandValueSerde)).split();

    // Movement_command.
    commandBranches.branch((k, v) -> {
      return DirectionParser.parse(v.getCOMMAND()) != null;
    }, Branched.withConsumer(stream -> stream.mapValues(v -> {
      return DirectionParser.parse(v.getCOMMAND());
    }).to(MOVEMENT_COMMAND_STREAM, Produced.with(Serdes.UUID(), movementCommandValueSerde))));

    // Inventory
    commandBranches.branch((k, v) -> {
      return StatusParser.parse(v.getCOMMAND()) != null;
    }, Branched.withConsumer(stream -> stream.mapValues(v -> {
      return StatusParser.parse(v.getCOMMAND());
    }).to(STATUS_COMMAND_STREAM, Produced.with(Serdes.UUID(), statusCommandValueSerde))));

    commandBranches.branch((k, v) -> {
      return PickupParser.parse(v.getCOMMAND()) != null;
    }, Branched.withConsumer(stream -> stream.mapValues(v -> {
      return PickupParser.parse(v.getCOMMAND());
    }).to(INVENTORY_COMMAND_STREAM, Produced.with(Serdes.UUID(), inventoryCommandValueSerde))));

    // Help
    commandBranches.branch((k, v) -> {
      return "HELP".equals(v.getCOMMAND());
    }, Branched.withConsumer(stream -> stream.mapValues(v -> {
      var responseString = new StringBuilder();
      responseString.append("Available commands are:\n");
      responseString.append("\tLOOK\n");
      responseString.append("\tGO NORTH\n");
      responseString.append("\tGO SOUTH\n");
      responseString.append("\tGO WEST\n");
      responseString.append("\tGO EAST\n");
      responseString.append("\tPICKUP <ITEM>\n");
      responseString.append("\tUSE <ITEM>\n");
      responseString.append("\tINVENTORY\n");
      responseString.append("\tHELP\n");

      var response = new ResponseValue();
      response.setSOURCE("Help");
      response.setRESPONSE(responseString.toString());
      return response;
    }).to(RESPONSES_STREAM, producedResponse)));

    // Fallback.
    commandBranches.defaultBranch(Branched.withConsumer(stream -> stream.mapValues(msg -> {
      var response = new ResponseValue();
      response.setSOURCE("Unknown Command");
      response.setRESPONSE(String.format("Unknown command: %s\n\nTry asking for HELP", msg.getCOMMAND()));
      return response;
    }).to(RESPONSES_STREAM, producedResponse)));

  }
}
