package io.confluent.kpay.rest_iq;

import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public interface StoreProvider<K, V> {
    ReadOnlyKeyValueStore<K, V> getStore();
}
