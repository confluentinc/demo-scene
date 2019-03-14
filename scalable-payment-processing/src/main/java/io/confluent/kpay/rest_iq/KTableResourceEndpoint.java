package io.confluent.kpay.rest_iq;

import io.confluent.kpay.util.GenericClassUtil;
import io.confluent.kpay.util.Pair;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class KTableResourceEndpoint<K,V> implements KTableResource<K, V> {

    private static final Logger log = LoggerFactory.getLogger(KTableResourceEndpoint.class);

    private StoreProvider<K, V> kvStoreProvider;
    private MicroRestService microRestService;

    public KTableResourceEndpoint(StoreProvider<K, V> KVStoreProvider) {
        this.kvStoreProvider = KVStoreProvider;
        // check the introspection is supported - must be created using anonymous construction for reflection to expose the rest endpoint correctly
        // this method will blow up if it can get Generic type info
        GenericClassUtil.getGenericType(this.getClass());

    };

    @Override
    public int size() {
        return (int) this.kvStoreProvider.getStore().approximateNumEntries();
    }

    @Override
    public Set<K> keySet() {
        HashSet<K> results = new HashSet<>();
        KeyValueIterator<K, V> all = this.kvStoreProvider.getStore().all();
        try {
            while (all.hasNext()) {
                results.add(all.next().key);
            }
        } finally {
            all.close();
        }
        return results;
    }

    @Override
    public V get(K k) {
        return this.kvStoreProvider.getStore().get(k);
    }

    @Override
    public List<Pair<K,V>> get(List<K> query) {
        List<Pair<K,V>> results = new ArrayList<>();
        for (K k : query) {
            V v = this.kvStoreProvider.getStore().get(k);
            results.add(new Pair(k, v));
        }
        return results;
    }


    public void start(Properties streamsConfig) {
        String property = streamsConfig.getProperty(StreamsConfig.APPLICATION_SERVER_CONFIG);

        if (property == null) {
            RuntimeException runtimeException = new RuntimeException("Not starting RestService for:" + streamsConfig.getProperty(StreamsConfig.APPLICATION_ID_CONFIG));
            log.warn("Cannot start, missing config", runtimeException);
            return;
        }
        microRestService = new MicroRestService();
        microRestService.start(this, property);
    }

    public void stop() {
        if (microRestService != null) {
            microRestService.stop();
        }
    }
}
