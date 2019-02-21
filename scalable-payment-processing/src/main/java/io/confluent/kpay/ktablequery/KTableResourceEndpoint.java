package io.confluent.kpay.ktablequery;

import io.confluent.kpay.util.Pair;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class KTableResourceEndpoint<K,V> implements KTableResource<K, V> {

    private static final Logger log = LoggerFactory.getLogger(KTableResourceEndpoint.class);

    private KVStoreProvider<K, V> KVStoreProvider;
    private MicroRestService microRestService;

    public KTableResourceEndpoint(KVStoreProvider<K, V> KVStoreProvider) {
        this.KVStoreProvider = KVStoreProvider;
    };

    @Override
    public int size() {
        return (int) this.KVStoreProvider.getStore().approximateNumEntries();
    }

    @Override
    public Set<K> keySet() {
        KeyValueIterator<K, V> all = this.KVStoreProvider.getStore().all();
        HashSet<K> results = new HashSet<>();
        while (all.hasNext()) {
            results.add(all.next().key);
        }
        all.close();
        return results;
    }

    @Override
    public V get(K k) {
        return this.KVStoreProvider.getStore().get(k);
    }

    @Override
    public List<Pair<K,V>> get(List<K> query) {
        List<Pair<K,V>> results = new ArrayList<>();
        for (K k : query) {
            V v = this.KVStoreProvider.getStore().get(k);
            results.add(new Pair(k, v));
        }
        return results;
    }


    public void start(Properties streamsConfig) {
        String property = streamsConfig.getProperty(StreamsConfig.APPLICATION_SERVER_CONFIG);

        if (property == null) {
            log.warn("Not starting RestService for:" + streamsConfig.getProperty(StreamsConfig.APPLICATION_ID_CONFIG));
            return;
            //throw new RuntimeException("Cannot start Rest endpoint, property is not set:" + StreamsConfig.APPLICATION_SERVER_CONFIG);
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
