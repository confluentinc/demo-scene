package io.confluent.demo.iq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import io.confluent.demo.RatedMovie;

@RestController
@RequestMapping("/iq")
public class InteractiveQueriesRestController {

  private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;
  private MetadataService metadataService;

  @Autowired
  public InteractiveQueriesRestController(StreamsBuilderFactoryBean streamsBuilderFactoryBean) {
    this.streamsBuilderFactoryBean = streamsBuilderFactoryBean;
  }

  @EventListener(ContextRefreshedEvent.class)
  public void method() {
    this.metadataService = new MetadataService(streamsBuilderFactoryBean.getKafkaStreams());
  }

  @RequestMapping("/instances")
  public List<HostStoreInfo> getStreamsMetadata() {
    return metadataService.streamsMetadata();
  }

  @RequestMapping(value = "/instances/{storeName}", method = RequestMethod.GET)
  public List<HostStoreInfo> streamsMetadataForStore(@PathVariable("storeName") final String store) {
    return metadataService.streamsMetadataForStore(store);
  }
  
  /*@RequestMapping(value = "/keyvalue/{storeName}/{key}")
  public RatedMovie byKey(@PathParam("storeName") final String storeName,
                          @PathParam("key") final String key) {

    final KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
    MetadataService metadataService = new MetadataService(kafkaStreams);
    final HostStoreInfo hostStoreInfo = metadataService.streamsMetadataForStoreAndKey(storeName, key);
    if (!thisHost(hostStoreInfo)) {
      return fetchByKey(hostStoreInfo, "iq/keyvalue/" + storeName + "/" + key);
    }

    // Lookup the KeyValueStore with the provided storeName
    final ReadOnlyKeyValueStore<String, Long>
        store =
        kafkaStreams.store(storeName, QueryableStoreTypes.keyValueStore());
    if (store == null) {
      throw new NotFoundException();
    }

    // Get the value from the store
    final Long value = store.get(key);
    if (value == null) {
      throw new NotFoundException();
    }
    return new KeyValueBean(key, value);
  }*/

  private RatedMovie fetchByKey(final HostStoreInfo host, final String path) {
    RestTemplate template = new RestTemplate();
    return template
        .getForObject(String.format("http://%s:%d/%s", host.getHost(), host.getPort(), path), RatedMovie.class);
  }

  /*private boolean thisHost(final HostStoreInfo host) {
    return host.getHost().equals(hostInfo.host()) &&
           host.getPort() == hostInfo.port();
  }*/
}
