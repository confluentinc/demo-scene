/*
 * Copyright Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.confluent.examples.streams.interactivequeries;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.apache.kafka.streams.state.WindowStoreIterator;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.apache.kafka.streams.StoreQueryParameters.fromNameAndType;

/**
 *  A simple REST proxy that runs embedded in the {@link WordCountInteractiveQueriesExample}. This is used to
 *  demonstrate how a developer can use the Interactive Queries APIs exposed by Kafka Streams to
 *  locate and query the State Stores within a Kafka Streams Application.
 */
@Path("state")
public class WordCountInteractiveQueriesRestService {

  private final KafkaStreams streams;
  private final MetadataService metadataService;
  private Server jettyServer;
  private final HostInfo hostInfo;
  private final Client client = ClientBuilder.newBuilder().register(JacksonFeature.class).build();
  private static final Logger log = LoggerFactory.getLogger(WordCountInteractiveQueriesRestService.class);

  WordCountInteractiveQueriesRestService(final KafkaStreams streams,
                                         final HostInfo hostInfo) {
    this.streams = streams;
    this.metadataService = new MetadataService(streams);
    this.hostInfo = hostInfo;

  }

  /**
   * Get a key-value pair from a KeyValue Store
   * @param storeName   the store to look in
   * @param key         the key to get
   * @return {@link KeyValueBean} representing the key-value pair
   */
  @GET
  @Path("/keyvalue/{storeName}/{key}")
  @Produces(MediaType.APPLICATION_JSON)
  public KeyValueBean byKey(@PathParam("storeName") final String storeName,
                            @PathParam("key") final String key) {

    final HostStoreInfo hostStoreInfo = streamsMetadataForStoreAndKey(storeName, key);
    if (!thisHost(hostStoreInfo)){
      return fetchByKey(hostStoreInfo, "state/keyvalue/"+storeName+"/"+key);
    }

    // Lookup the KeyValueStore with the provided storeName
    final ReadOnlyKeyValueStore<String, Long> store =
            streams.store(fromNameAndType(storeName, QueryableStoreTypes.keyValueStore()));
    if (store == null) {
      throw new NotFoundException();
    }

    // Get the value from the store
    final Long value = store.get(key);
    if (value == null) {
      throw new NotFoundException();
    }
    return new KeyValueBean(key, value);
  }

  private KeyValueBean fetchByKey(final HostStoreInfo host, final String path) {
    return client.target(String.format("http://%s:%d/%s", host.getHost(), host.getPort(), path))
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get(new GenericType<KeyValueBean>() {
            });
  }

  /**
   * Get all of the key-value pairs available in a store
   * @param storeName   store to query
   * @return A List of {@link KeyValueBean}s representing all of the key-values in the provided
   * store
   */
  @GET()
  @Path("/keyvalues/{storeName}/all")
  @Produces(MediaType.APPLICATION_JSON)
  public List<KeyValueBean> allForStore(@PathParam("storeName") final String storeName) {
    return rangeForKeyValueStore(storeName, ReadOnlyKeyValueStore::all);
  }

  /**
   * Get all of the key-value pairs that have keys within the range from...to
   * @param storeName   store to query
   * @param from        start of the range (inclusive)
   * @param to          end of the range (inclusive)
   * @return A List of {@link KeyValueBean}s representing all of the key-values in the provided
   * store that fall withing the given range.
   */
  @GET()
  @Path("/keyvalues/{storeName}/range/{from}/{to}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<KeyValueBean> keyRangeForStore(@PathParam("storeName") final String storeName,
                                             @PathParam("from") final String from,
                                             @PathParam("to") final String to) {
    return rangeForKeyValueStore(storeName, store -> store.range(from, to));
  }

  /**
   * Query a window store for key-value pairs representing the value for a provided key within a
   * range of windows
   * @param storeName   store to query
   * @param key         key to look for
   * @param from        time of earliest window to query
   * @param to          time of latest window to query
   * @return A List of {@link KeyValueBean}s representing the key-values for the provided key
   * across the provided window range.
   */
  @GET()
  @Path("/windowed/{storeName}/{key}/{from}/{to}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<KeyValueBean> windowedByKey(@PathParam("storeName") final String storeName,
                                          @PathParam("key") final String key,
                                          @PathParam("from") final Long from,
                                          @PathParam("to") final Long to) {

    // Lookup the WindowStore with the provided storeName
    final ReadOnlyWindowStore<String, Long> store =
            streams.store(fromNameAndType(storeName, QueryableStoreTypes.windowStore()));
    if (store == null) {
      throw new NotFoundException();
    }

    // fetch the window results for the given key and time range
    final WindowStoreIterator<Long> results = store.fetch(key, Instant.ofEpochMilli(from), Instant.ofEpochMilli(to));

    final List<KeyValueBean> windowResults = new ArrayList<>();
    while (results.hasNext()) {
      final KeyValue<Long, Long> next = results.next();
      // convert the result to have the window time and the key (for display purposes)
      windowResults.add(new KeyValueBean(key + "@" + next.key, next.value));
    }
    return windowResults;
  }

  /**
   * Get the metadata for all of the instances of this Kafka Streams application
   * @return List of {@link HostStoreInfo}
   */
  @GET()
  @Path("/instances")
  @Produces(MediaType.APPLICATION_JSON)
  public List<HostStoreInfo> streamsMetadata() {
    return metadataService.streamsMetadata();
  }

  /**
   * Get the metadata for all instances of this Kafka Streams application that currently
   * has the provided store.
   * @param store   The store to locate
   * @return  List of {@link HostStoreInfo}
   */
  @GET()
  @Path("/instances/{storeName}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<HostStoreInfo> streamsMetadataForStore(@PathParam("storeName") final String store) {
    return metadataService.streamsMetadataForStore(store);
  }

  /**
   * Find the metadata for the instance of this Kafka Streams Application that has the given
   * store and would have the given key if it exists.
   * @param store   Store to find
   * @param key     The key to find
   * @return {@link HostStoreInfo}
   */
  @GET()
  @Path("/instance/{storeName}/{key}")
  @Produces(MediaType.APPLICATION_JSON)
  public HostStoreInfo streamsMetadataForStoreAndKey(@PathParam("storeName") final String store,
                                                     @PathParam("key") final String key) {
    return metadataService.streamsMetadataForStoreAndKey(store, key, new StringSerializer());
  }

  /**
   * Performs a range query on a KeyValue Store and converts the results into a List of
   * {@link KeyValueBean}
   * @param storeName       The store to query
   * @param rangeFunction   The range query to run, i.e., all, from(start, end)
   * @return  List of {@link KeyValueBean}
   */
  private List<KeyValueBean> rangeForKeyValueStore(final String storeName,
                                                   final Function<ReadOnlyKeyValueStore<String, Long>,
                                                           KeyValueIterator<String, Long>> rangeFunction) {

    // Get the KeyValue Store
    final ReadOnlyKeyValueStore<String, Long> store =
            streams.store(fromNameAndType(storeName, QueryableStoreTypes.keyValueStore()));
    final List<KeyValueBean> results = new ArrayList<>();
    // Apply the function, i.e., query the store
    final KeyValueIterator<String, Long> range = rangeFunction.apply(store);

    // Convert the results
    while (range.hasNext()) {
      final KeyValue<String, Long> next = range.next();
      results.add(new KeyValueBean(next.key, next.value));
    }

    return results;
  }

  private boolean thisHost(final HostStoreInfo host) {
    return host.getHost().equals(hostInfo.host()) &&
            host.getPort() == hostInfo.port();
  }

  /**
   * Start an embedded Jetty Server on the given port
   * @param port    port to run the Server on
   * @throws Exception if jetty can't start
   */
  void start(final int port) throws Exception {
    final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");

    jettyServer = new Server();
    jettyServer.setHandler(context);

    final ResourceConfig rc = new ResourceConfig();
    rc.register(this);
    rc.register(JacksonFeature.class);

    final ServletContainer sc = new ServletContainer(rc);
    final ServletHolder holder = new ServletHolder(sc);
    context.addServlet(holder, "/*");

    final ServerConnector connector = new ServerConnector(jettyServer);
    connector.setHost(hostInfo.host());
    connector.setPort(port);
    jettyServer.addConnector(connector);

    try {
      jettyServer.start();
    } catch (final java.net.SocketException exception) {
      log.error("Unavailable: " + hostInfo.host() + ":" + hostInfo.port());
      throw new Exception(exception.toString());
    }
  }

  /**
   * Stop the Jetty Server
   * @throws Exception if jetty can't stop
   */
  void stop() throws Exception {
    if (jettyServer != null) {
      jettyServer.stop();
    }
  }

}

