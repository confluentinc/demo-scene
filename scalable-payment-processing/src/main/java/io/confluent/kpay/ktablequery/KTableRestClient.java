package io.confluent.kpay.ktablequery;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import io.confluent.kpay.util.GenericClassUtil;
import io.confluent.kpay.util.Pair;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Rest Client aggregator that uses StreamsMetadata for the associated KTable Stores that are exposed via remote Rest Endpoints
 * @param <K> key
 * @param <V> value
 */
public class KTableRestClient<K,V> extends AbstractKTableClient<K,V> implements KTableResource<K,V> {

    Class keyClass;
    Class valueClass;

    Client client = ClientBuilder.newClient();
    private KafkaStreams streams;
    private String store;

    public KTableRestClient(KafkaStreams streams, String store) {
        this.streams = streams;
        this.store = store;
        client.register(JacksonJsonProvider.class);

        Class[] genericTypes = GenericClassUtil.getGenericTypes(this.getClass());
        keyClass = genericTypes[0];
        valueClass = genericTypes[1];
    }

    @Override
    public int size() {
        int result = 0;
        MetadataService metadataService = new MetadataService(streams);
        List<HostStoreInfo> hostStoreInfos = metadataService.streamsMetadataForStore(store);

        for (HostStoreInfo hostStoreInfo : hostStoreInfos) {
            result += client.target(hostStoreInfo.getAddress()).path("/ktable/size")
                    .request(MediaType.APPLICATION_JSON)
                    .get(int.class);
        }
        return result;
    }

    @Override
    public Set<K> keySet() {

        MetadataService metadataService = new MetadataService(streams);
        List<HostStoreInfo> hostStoreInfos = metadataService.streamsMetadataForStore(store);

        HashSet<K> result = new HashSet<>();
        for (HostStoreInfo hostStoreInfo : hostStoreInfos) {
            result.addAll(client.target(hostStoreInfo.getAddress()).path("/ktable/keys")
                    .request(MediaType.APPLICATION_JSON)
                    .get(Set.class));
        }
        return result;
    }

    @Override
    public V get(K k) {
        MetadataService metadataService = new MetadataService(streams);
        HostStoreInfo hostStoreInfo = metadataService.streamsMetadataForStoreAndKey(store, (String) k, new StringSerializer());

        WebTarget tsTarget = client.target(hostStoreInfo.getAddress()).path("/ktable/get");

        V response =
                (V) tsTarget.request(MediaType.APPLICATION_JSON_TYPE)
                        .post(Entity.entity(k, MediaType.APPLICATION_JSON),
                                valueClass);
        return response;
    }

    @Override
    public List<Pair<K, V>> get(List<K> query) {
        MetadataService metadataService = new MetadataService(streams);
        List<HostStoreInfo> hostStoreInfos = metadataService.streamsMetadataForStore(store);

        // sub optimal - sending all keys to each host
        List result = new ArrayList<Pair<K, V>>();
        for (HostStoreInfo hostStoreInfo : hostStoreInfos) {
            result.addAll(client.target(hostStoreInfo.getAddress()).path("/ktable/getQuery")
                    .request(MediaType.APPLICATION_JSON)
                    .get(List.class));
        }
        return result;

    }
}
