package io.confluent.kpay.ktablequery;

import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public interface StoreProvider<K, V> {
    ReadOnlyKeyValueStore<K, V> getStore();
}
