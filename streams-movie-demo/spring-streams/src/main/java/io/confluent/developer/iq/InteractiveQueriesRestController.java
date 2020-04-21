package io.confluent.developer.iq;

import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.confluent.developer.Movie;
import io.confluent.developer.Parser;
import io.confluent.developer.RatedMovie;

@RestController
@RequestMapping("/iq")
public class InteractiveQueriesRestController {

  private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;
  private final HostInfo hostInfo;
  private MetadataService metadataService;

  @Autowired
  public InteractiveQueriesRestController(StreamsBuilderFactoryBean streamsBuilderFactoryBean, HostInfo hostInfoBean) {
    this.streamsBuilderFactoryBean = streamsBuilderFactoryBean;
    this.hostInfo = hostInfoBean;
  }

  @EventListener(ContextRefreshedEvent.class)
  public void method() {
    this.metadataService = new MetadataService(streamsBuilderFactoryBean.getKafkaStreams());
  }

  @RequestMapping("/instances")
  public List<HostStoreInfo> getStreamsMetadata() {
    return metadataService.streamsMetadata();
  }

  @RequestMapping(value = "/instance/{storeName}", method = RequestMethod.GET)
  public List<HostStoreInfo> streamsMetadataForStore(@PathVariable("storeName") final String store) {
    return metadataService.streamsMetadataForStore(store);
  }

  @RequestMapping("/instance/{storeName}/{key}")
  public HostStoreInfo streamsMetadataForStoreAndKey(@PathVariable("storeName") final String store,
                                                     @PathVariable("key") final String key) {
    return metadataService.streamsMetadataForStoreAndKey(store, key, new StringSerializer());
  }

  @RequestMapping(value = "/keyvalue/{storeName}/{key}")
  public KeyValueBean<?, ?> byKey(@PathVariable("storeName") final String storeName,
                                  @PathVariable("key") final String key) {

    final HostStoreInfo hostStoreInfo = streamsMetadataForStoreAndKey(storeName, key);
    if (!thisHost(hostStoreInfo)) {
      return fetchByKey(hostStoreInfo, "iq/keyvalue/" + storeName + "/" + key);
    }

    final long l = Long.parseLong(key);
    switch (storeName) {
      case "movies-store":
        final ReadOnlyKeyValueStore<Long, Movie>
            movieReadOnlyKeyValueStore =
            metadataService.getKafkaStreams().store(storeName, QueryableStoreTypes.keyValueStore());
        final Movie movie = movieReadOnlyKeyValueStore.get(l);
        // TODO
        // Workaround for @JsonComponent isn't working ;(
        return new KeyValueBean<>(l, Parser.toJson(movie));
      case "rated-movies-store":
        final ReadOnlyKeyValueStore<Long, RatedMovie>
            ratedMoviesStore =
            metadataService.getKafkaStreams().store(storeName, QueryableStoreTypes.keyValueStore());
        final RatedMovie ratedMovie = ratedMoviesStore.get(l);
        return new KeyValueBean<>(l, Parser.toJson(ratedMovie));
      default:
        throw new IllegalArgumentException("Unknown store name " + storeName);
    }

    /*// Lookup the KeyValueStore with the provided storeName
    final ReadOnlyKeyValueStore<String, Long>
        store =
        metadataService.getKafkaStreams().store(storeName, QueryableStoreTypes.keyValueStore());
    if (store == null) {
      throw new RuntimeException("not found");
    }

    // Get the value from the store
    final Long value = store.get(key);
    if (value == null) {
      throw new RuntimeException("not found");
    }
    return new KeyValueBean(key, value);*/
  }

  private List<KeyValueBean> rangeForKeyValueStore(final String storeName,
                                                   final Function<ReadOnlyKeyValueStore<String, Long>,
                                                       KeyValueIterator<String, Long>> rangeFunction) {

    // Get the KeyValue Store
    final ReadOnlyKeyValueStore<String, Long>
        store =
        metadataService.getKafkaStreams().store(storeName, QueryableStoreTypes.keyValueStore());
    final List<KeyValueBean> results = new ArrayList<>();
    // Apply the function, i.e., query the store
    final KeyValueIterator<String, Long> range = rangeFunction.apply(store);

    // Convert the results
    while (range.hasNext()) {
      final KeyValue<String, Long> next = range.next();
      results.add(new KeyValueBean<>(next.key, next.value));
    }

    return results;
  }

  private KeyValueBean fetchByKey(final HostStoreInfo host, final String path) {
    RestTemplate template = new RestTemplate();
    final String remoteUrl = String.format("http://%s:%d/%s", host.getHost(), host.getPort(), path);
    return template
        .getForObject(remoteUrl, KeyValueBean.class);
  }

  private boolean thisHost(final HostStoreInfo host) {
    return host.getHost().equals(hostInfo.host()) &&
           host.getPort() == hostInfo.port();
  }


}
