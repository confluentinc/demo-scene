package io.confluent.kpay.rest_iq;

import io.confluent.kpay.util.GenericClassUtil;
import io.confluent.kpay.util.Pair;
import java.util.*;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindowKTableResourceEndpoint<K,V> implements WindowKTableResource<K, V> {

    private static final Logger log = LoggerFactory.getLogger(WindowKTableResourceEndpoint.class);

    private WindowStoreProvider<K, V> kvStoreProvider;
    private MicroRestService microRestService;

    public WindowKTableResourceEndpoint(WindowStoreProvider<K, V> KVStoreProvider) {
        this.kvStoreProvider = KVStoreProvider;
        // check the introspection is supported - must be created using anonymous construction for reflection to expose the rest endpoint correctly
        // this method will blow up if it can get Generic type info
        GenericClassUtil.getGenericType(this.getClass());

    };

    @Override
    public int size() {
        int count = 0;
        ReadOnlyWindowStore<K, V> store = this.kvStoreProvider.getStore();
        KeyValueIterator<Windowed<K>, V> all = store.all();
        try {
            while (all.hasNext()) {
                all.next();
                count++;
            }
        } finally {
            all.close();
        }
        return count;
    }

    @Override
    public Set<K> keySet() {
        HashSet<K> results = new HashSet<>();
        KeyValueIterator<Windowed<K>, V> all = this.kvStoreProvider.getStore().all();
        try {
            while (all.hasNext()) {
                KeyValue<Windowed<K>, V> next = all.next();
                results.add(next.key.key());
            }
        } finally {
            all.close();
        }
        return results;
    }

    @Override
    public V get(K k) {
        return this.kvStoreProvider.getStore().fetch(k, System.currentTimeMillis());
//        WindowStoreIterator<V> iterator = this.kvStoreProvider.getStore().fetch(k, 0, System.currentTimeMillis());
//
//        try {
//            // TODO: fix me - erasure
//            while (iterator.hasNext()) {
//                KeyValue<Long, V> next = iterator.next();
//                v = next.value;
//            }
//        } finally {
//            iterator.close();
//        }
//        return v;
    }

    @Override
    public List<Pair<K,V>> get(List<K> query) {
        Set<Pair<K,V>> results = new HashSet<>();
        KeyValueIterator<Windowed<K>, V> all = this.kvStoreProvider.getStore().all();
        try {
            // TODO: fix me - yuck
            while (all.hasNext()) {
                KeyValue<Windowed<K>, V> next = all.next();
                if (query.contains(next.key.key())) {
                    results.add(new Pair(next.key.key(), next.value));
                }
            }
        } finally {
            all.close();
        }
        return new ArrayList<>(results);
    }


    public void start(Properties streamsConfig) {
        String property = streamsConfig.getProperty(StreamsConfig.APPLICATION_SERVER_CONFIG);

        if (property == null) {
            RuntimeException runtimeException = new RuntimeException("Not starting RestService for:" + streamsConfig.getProperty(StreamsConfig.APPLICATION_ID_CONFIG));
            log.warn("Cannot startProcessors, missing config", runtimeException);
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
