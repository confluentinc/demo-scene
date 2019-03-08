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

    private final String rootPath;
    private Class keyClass;
    private Class valueClass;

    Client client = ClientBuilder.newClient();
    private KafkaStreams streams;
    private String store;

    public KTableRestClient(boolean isKTable, KafkaStreams streams, String store) {
        this.rootPath = isKTable ? "ktable" : "windowktable";
        this.streams = streams;
        this.store = store;
        client.register(JacksonJsonProvider.class);

        Class[] genericTypes = GenericClassUtil.getGenericType(this.getClass());
        keyClass = genericTypes[0];
        valueClass = genericTypes[1];
    }

    @Override
    public int size() {
        int result = 0;

        for (HostStoreInfo hostStoreInfo : getHostStoreInfos()) {
            result += client.target(hostStoreInfo.getAddress()).path(String.format("/%s/size", rootPath))
                    .request(MediaType.APPLICATION_JSON)
                    .get(int.class);
        }
        return result;
    }

    @Override
    public Set<K> keySet() {

        HashSet<K> result = new HashSet<>();
        for (HostStoreInfo hostStoreInfo : getHostStoreInfos()) {
            result.addAll(client.target(hostStoreInfo.getAddress()).path(String.format("/%s/keys", rootPath))
                    .request(MediaType.APPLICATION_JSON)
                    .get(Set.class));
        }
        return result;
    }

    @Override
    public V get(K k) {
        HostStoreInfo hostStoreInfo = getHostStoreInfoForKey((String) k);

        WebTarget tsTarget = client.target(hostStoreInfo.getAddress()).path(String.format("/%s/get", rootPath));

        V response =
                (V) tsTarget.request(MediaType.APPLICATION_JSON_TYPE)
                        .post(Entity.entity(k, MediaType.APPLICATION_JSON),
                                valueClass);
        return response;
    }

    @Override
    public List<Pair<K, V>> get(List<K> query) {
        List<HostStoreInfo> hostStoreInfos = getHostStoreInfos();

        // sub optimal - sending all keys to each host
        List result = new ArrayList<Pair<K, V>>();
        for (HostStoreInfo hostStoreInfo : hostStoreInfos) {
            result.addAll(client.target(hostStoreInfo.getAddress()).path(String.format("/%s/getQuery", rootPath))
                    .request(MediaType.APPLICATION_JSON)
                    .get(List.class));
        }
        return result;
    }

    /**
     * Protected for testing purposes
     * @return
     */
    protected List<HostStoreInfo> getHostStoreInfos() {
        return new MetadataService(streams).streamsMetadataForStore(store);
    }
    protected HostStoreInfo getHostStoreInfoForKey(String k) {
        return new MetadataService(streams).streamsMetadataForStoreAndKey(store, k, new StringSerializer());
    }

    public void stop() {
        client.close();
    }
}
