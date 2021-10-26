package io.confluent.developer.adventure;

import static io.confluent.developer.adventure.Constants.*;
import static io.confluent.developer.adventure.Constants.MOVEMENT_COMMAND_STREAM;
import static io.confluent.developer.adventure.Constants.RESPONSES_STREAM;
import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

public class MovementProcessor {
  private static Logger logger = LoggerFactory.getLogger(MovementProcessor.class);

  public static KTable<UUID, LocationData> buildStreams(Map<String, String> schemaRegistryProps,
      StreamsBuilder builder) {
    Serde<LocationDataValue> locationDataValueSerde = new SpecificAvroSerde<>();
    locationDataValueSerde.configure(schemaRegistryProps, false);
    Runtime.getRuntime().addShutdownHook(new Thread(locationDataValueSerde::close));

    GlobalKTable<Position, LocationDataValue> locationDataTable =
        builder.globalTable(LOCATION_DATA_STREAM, Materialized.with(PositionSerde.Serde, locationDataValueSerde));

    Serde<MovementCommandValue> movementCommandValueSerde = new SpecificAvroSerde<>();
    movementCommandValueSerde.configure(schemaRegistryProps, false);

    Serde<LocationData> locationDataSerde = new SpecificAvroSerde<>();
    locationDataSerde.configure(schemaRegistryProps, false);

    SpecificAvroSerde<ResponseValue> responseValueSerde = new SpecificAvroSerde<>();
    responseValueSerde.configure(schemaRegistryProps, false);
    Produced<UUID, ResponseValue> producedResponse = Produced.with(Serdes.UUID(), responseValueSerde);

    logger.info("Defining user location stream.");
    KTable<UUID, Position> userPositionTable =
        builder
          .stream(MOVEMENT_COMMAND_STREAM, Consumed.with(Serdes.UUID(), movementCommandValueSerde))
          .mapValues(v -> {
            switch (v.getDIRECTION()) {
            case "START":
              return new Position(0, 0);
            case "NORTH":
              return new Position(0, 1);
            case "SOUTH":
              return new Position(0, -1);
            case "WEST":
              return new Position(-1, 0);
            case "EAST":
              return new Position(1, 0);
            default:
              return null;
            }
          })
          .filter((k, v) -> v != null)
          .groupByKey(Grouped.with(Serdes.UUID(), PositionSerde.Serde))
          .reduce(new PositionReducer(), Materialized.with(Serdes.UUID(), PositionSerde.Serde));

    // Location description stream.
    logger.info("Defining the user location change response.");
    userPositionTable.toStream().mapValues(position -> {
      ResponseValue response = new ResponseValue();
      response.setSOURCE("Position");
      response.setRESPONSE(String.format("You moved to %s", position));
      return response;
    }).to(RESPONSES_STREAM, producedResponse);

    // Location description stream.
    logger.info("Defining the user location description response.");
    KStream<UUID, LocationData> userLocationChanges =
        userPositionTable
          .toStream()
          .leftJoin(locationDataTable, (uuid, position) -> position,
              (position, locationData) -> new LocationData(position.getX(), position.getY(),
                  locationData == null ? "" : locationData.getDESCRIPTION(),
                  locationData == null ? emptyList() : locationData.getOBJECTS()));

    userLocationChanges.mapValues(locationData -> {
      ResponseValue response = new ResponseValue();
      response.setSOURCE("Location Description");

      if (locationData == null) {
        response.setRESPONSE("You have fallen off the edge of the map. Best head back the way you came!");
      } else {
        StringBuilder responseString = new StringBuilder();
        responseString.append(locationData.getDESCRIPTION());

        List<String> items = locationData.getOBJECTS();
        if (!(items == null || items.isEmpty())) {
          responseString.append("\n\n");
          responseString.append("You can see: ");
          responseString.append(items.stream().collect(Collectors.joining(", ")));
        }

        response.setRESPONSE(responseString.toString());
      }

      return response;
    }).to(RESPONSES_STREAM, producedResponse);

    KTable<UUID, LocationData> latestUserLocation =
        userLocationChanges.groupByKey(Grouped.with(Serdes.UUID(), locationDataSerde)).reduce((a, b) -> b);
    return latestUserLocation;
  }
}
