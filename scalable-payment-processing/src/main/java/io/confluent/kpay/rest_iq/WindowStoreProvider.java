package io.confluent.kpay.rest_iq;

import org.apache.kafka.streams.state.ReadOnlyWindowStore;

public interface WindowStoreProvider<K, V> {
    ReadOnlyWindowStore<K, V> getStore();
}
