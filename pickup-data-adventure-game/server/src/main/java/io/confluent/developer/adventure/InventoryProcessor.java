package io.confluent.developer.adventure;

import static io.confluent.developer.adventure.Constants.INVENTORY_COMMAND_STREAM;
import static io.confluent.developer.adventure.Constants.INVENTORY_STREAM;
import static io.confluent.developer.adventure.Constants.ITEM_RULES_STREAM;
import static io.confluent.developer.adventure.Constants.RESPONSES_STREAM;
import static io.confluent.developer.adventure.Constants.STATUS_COMMAND_STREAM;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.BranchedKStream;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

public class InventoryProcessor {
  private static Logger logger = LoggerFactory.getLogger(InventoryProcessor.class);

  public static void buildStreams(Map<String, String> schemaRegistryProps, StreamsBuilder builder,
      KTable<UUID, LocationData> latestUserLocation) {
    var inventoryCommandValueSerde = new SpecificAvroSerde<InventoryCommandValue>();
    inventoryCommandValueSerde.configure(schemaRegistryProps, false);
    Runtime.getRuntime().addShutdownHook(new Thread(inventoryCommandValueSerde::close));

    var inventoryValueSerde = new SpecificAvroSerde<InventoryValue>();
    inventoryValueSerde.configure(schemaRegistryProps, false);
    Runtime.getRuntime().addShutdownHook(new Thread(inventoryValueSerde::close));

    var responseValueSerde = new SpecificAvroSerde<ResponseValue>();
    responseValueSerde.configure(schemaRegistryProps, false);
    Runtime.getRuntime().addShutdownHook(new Thread(responseValueSerde::close));

    var knapsackSerde = new SpecificAvroSerde<Knapsack>();
    knapsackSerde.configure(schemaRegistryProps, false);
    Runtime.getRuntime().addShutdownHook(new Thread(knapsackSerde::close));

    var itemRulesValueSerde = new SpecificAvroSerde<ItemRulesValue>();
    itemRulesValueSerde.configure(schemaRegistryProps, false);
    Runtime.getRuntime().addShutdownHook(new Thread(itemRulesValueSerde::close));

    var statusCommandValueSerde = new SpecificAvroSerde<StatusCommandValue>();
    statusCommandValueSerde.configure(schemaRegistryProps, false);
    Runtime.getRuntime().addShutdownHook(new Thread(statusCommandValueSerde::close));

    Produced<UUID, ResponseValue> producedResponse = Produced.with(Serdes.UUID(), responseValueSerde);

    final GlobalKTable<String, ItemRulesValue> itemRulesTable =
        builder.globalTable(ITEM_RULES_STREAM, Materialized.with(Serdes.String(), itemRulesValueSerde));

    KStream<UUID, Pair<String, Boolean>> newInventoryStream =
        builder
          .stream(INVENTORY_COMMAND_STREAM, Consumed.with(Serdes.UUID(), inventoryCommandValueSerde))
          .filter((k, v) -> "PICKUP".equals(v.getACTION()))
          .join(latestUserLocation, (inventoryCommand, locationData) -> {
            String requestedObject = inventoryCommand.getITEM();
            List<String> objects = locationData.getOBJECTS();
            boolean isAvailable = objects != null && objects.contains(requestedObject);
            return new Pair<String, Boolean>(requestedObject, isAvailable);
          });

    newInventoryStream
      .split()
      .branch((k, pair) -> pair.getValue1(), Branched.withConsumer(stream -> stream.mapValues(pair -> {
        var v = new InventoryValue();
        v.setITEM(pair.getValue0());
        v.setHELD(true);
        return v;
      }).to(INVENTORY_STREAM, Produced.with(Serdes.UUID(), inventoryValueSerde))))
      .defaultBranch(Branched.withConsumer(stream -> stream.mapValues(pair -> {
        var response = new ResponseValue();
        response.setSOURCE("Inventory");
        response.setRESPONSE(String.format("You cannot pick up: %s", pair.getValue0()));
        return response;
      }).to(RESPONSES_STREAM, Produced.with(Serdes.UUID(), responseValueSerde))));

    builder
      .stream(INVENTORY_STREAM, Consumed.with(Serdes.UUID(), inventoryValueSerde))
      .filter((k, inventoryValue) -> inventoryValue.getHELD())
      .mapValues((k, inventoryValue) -> {
        var response = new ResponseValue();
        response.setSOURCE("Inventory");
        response.setRESPONSE(String.format("You pick up: %s.", inventoryValue.getITEM()));
        return response;
      })
      .to(RESPONSES_STREAM, Produced.with(Serdes.UUID(), responseValueSerde));

    KTable<UUID, Knapsack> inventoryTable =
        builder
          .stream(INVENTORY_STREAM, Consumed.with(Serdes.UUID(), inventoryValueSerde))
          .groupByKey(Grouped.with(Serdes.UUID(), inventoryValueSerde))
          .aggregate(() -> {
            var knapsack = new Knapsack();
            knapsack.setOBJECTS(new ArrayList<String>());
            return knapsack;
          }, (k, item, knapsack) -> {
            List<String> currentObjects = knapsack.getOBJECTS();
            if (item.getHELD() && !currentObjects.contains(item.getITEM())) {
              logger.info("Adding {} to {}", item, knapsack);
              currentObjects.add(item.getITEM());
            } else {
              var filteredObjects = new ArrayList<String>(knapsack.getOBJECTS());
              logger.info("Removing {} from {}", item, knapsack);
              filteredObjects.removeIf(item.getITEM()::equals);
              knapsack.setOBJECTS(filteredObjects);
            }
            return knapsack;
          }, Materialized.with(Serdes.UUID(), knapsackSerde));

    KStream<UUID, UseItemJoin> useInventoryStream =
        builder
          .stream(INVENTORY_COMMAND_STREAM, Consumed.with(Serdes.UUID(), inventoryCommandValueSerde))
          .filter((k, v) -> "USE".equals(v.getACTION()))
          .join(inventoryTable, (inventoryCommand, inventory) -> new Pair<>(inventoryCommand, inventory))
          .join(latestUserLocation,
              (pair, locationData) -> new Triplet<>(pair.getValue0(), pair.getValue1(), locationData))
          .leftJoin(itemRulesTable, (k, v) -> v.getValue0().getITEM(), (triplet,
              itemRule) -> new UseItemJoin(triplet.getValue0(), triplet.getValue1(), triplet.getValue2(), itemRule));

    BranchedKStream<UUID, UseItemJoin> useItemBranches = useInventoryStream.split();

    useItemBranches.branch((k, useItemJoin) -> {
      boolean isAvailable = useItemJoin.knapsack().getOBJECTS().contains(useItemJoin.inventoryCommand().getITEM());
      return !isAvailable;
    }, Branched.withConsumer(stream -> stream.mapValues(useItemJoin -> {
      var response = new ResponseValue();
      response.setSOURCE("Inventory Use");
      response.setRESPONSE("You do not have that item to use.");
      return response;
    }).to(RESPONSES_STREAM, Produced.with(Serdes.UUID(), responseValueSerde))));

    useItemBranches.branch((k, v) -> {
      if (v.itemRule() == null) {
        return true;
      }

      boolean matchingX = v.locationData().getX() == v.itemRule().getX();
      boolean matchingY = v.locationData().getY() == v.itemRule().getY();
      boolean isWrongLocation = !(matchingX && matchingY);
      return isWrongLocation;
    }, Branched.withConsumer(stream -> stream.mapValues(v -> {
      var response = new ResponseValue();
      response.setSOURCE("Inventory Use");
      response.setRESPONSE("You cannot use that item here.");
      return response;
    }).to(RESPONSES_STREAM, Produced.with(Serdes.UUID(), responseValueSerde))));

    useItemBranches.defaultBranch(Branched.withConsumer(stream -> {
      // Add the new item to our inventory.
      stream.mapValues(v -> {
        return new InventoryValue(v.itemRule().getGENERATESITEM(), true);
      }).to(INVENTORY_STREAM, Produced.with(Serdes.UUID(), inventoryValueSerde));

      // Remove the old item from our inventory.
      stream.mapValues(v -> {
        return new InventoryValue(v.inventoryCommand().getITEM(), false);
      }).to(INVENTORY_STREAM, Produced.with(Serdes.UUID(), inventoryValueSerde));

      // Send the description to the user.
      stream.mapValues(v -> {
        var response = new ResponseValue();
        response.setSOURCE("Inventory Use");
        response.setRESPONSE(v.itemRule().getDESCRIPTION());
        return response;
      }).to(RESPONSES_STREAM, Produced.with(Serdes.UUID(), responseValueSerde));
    }));

    BranchedKStream<UUID, StatusCommandValue> statusCommandBranches =
        builder.stream(STATUS_COMMAND_STREAM, Consumed.with(Serdes.UUID(), statusCommandValueSerde)).split();

    statusCommandBranches.branch((k, v) -> {
      return "INVENTORY".equals(v.getCOMMAND());
    }, Branched.withConsumer(stream -> stream.leftJoin(inventoryTable, (command, items) -> items).mapValues(items -> {
      var response = new ResponseValue();
      response.setSOURCE("Inventory Check");

      if (items == null || items.getOBJECTS().isEmpty()) {
        response.setRESPONSE(String.format("Your knapsack is empty."));
      } else {
        response.setRESPONSE(String.format("Your knapsack contains: %s.", items.getOBJECTS()));
      }

      return response;
    }).to(RESPONSES_STREAM, producedResponse)));

  }
}
