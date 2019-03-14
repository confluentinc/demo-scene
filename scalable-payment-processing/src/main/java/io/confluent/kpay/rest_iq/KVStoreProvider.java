package io.confluent.kpay.rest_iq;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public class KVStoreProvider<K, V> implements StoreProvider {

    private KafkaStreams streams;
    private KTable<K, V> table;

    public KVStoreProvider(KafkaStreams streams, KTable<K, V> table) {
        this.streams = streams;
        this.table = table;
    }

    public ReadOnlyKeyValueStore<K, V> getStore() {
        return streams.store(table.queryableStoreName(), QueryableStoreTypes.keyValueStore());
    }
}
