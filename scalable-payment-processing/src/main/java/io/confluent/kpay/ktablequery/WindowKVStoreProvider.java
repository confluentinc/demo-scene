package io.confluent.kpay.ktablequery;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;

public class WindowKVStoreProvider<K, V> implements WindowStoreProvider {

    private KafkaStreams streams;
    private KTable<Windowed<K>, V> table;

    public WindowKVStoreProvider(KafkaStreams streams, KTable<Windowed<K>, V> table) {
        this.streams = streams;
        this.table = table;
    }

    public ReadOnlyWindowStore<K, V> getStore() {
        return streams.store(table.queryableStoreName(), QueryableStoreTypes.windowStore());
    }
}
