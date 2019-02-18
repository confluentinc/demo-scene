package io.confluent.kpay.util;

import org.apache.kafka.streams.state.KeyValueIterator;

import java.util.*;

public class ReadonlyMapResourceEndpoint<K,V> implements ReadOnlyMapResource<K, V> {

    private StoreProvider<K, V> storeProvider;

    public ReadonlyMapResourceEndpoint(StoreProvider<K, V> storeProvider) {
        this.storeProvider = storeProvider;
    };

    @Override
    public int size() {
        return (int) this.storeProvider.getStore().approximateNumEntries();
    }

    @Override
    public Set<K> keySet() {
        KeyValueIterator<K, V> all = this.storeProvider.getStore().all();
        HashSet<K> results = new HashSet<>();
        while (all.hasNext()) {
            results.add(all.next().key);
        }
        all.close();
        return results;
    }

    @Override
    public V get(K k) {
        return this.storeProvider.getStore().get(k);
    }

    @Override
    public List<Pair<K,V>> get(List<K> query) {
        List<Pair<K,V>> results = new ArrayList<>();
        for (K k : query) {
            V v = this.storeProvider.getStore().get(k);
            results.add(new Pair(k, v));
        }
        return results;
    }
}
