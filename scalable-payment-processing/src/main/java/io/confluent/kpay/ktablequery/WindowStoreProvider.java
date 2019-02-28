package io.confluent.kpay.ktablequery;

import org.apache.kafka.streams.state.ReadOnlyWindowStore;

public interface WindowStoreProvider<K, V> {
    ReadOnlyWindowStore<K, V> getStore();
}
