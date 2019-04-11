package io.confluent.kpay.metrics;

import io.confluent.kpay.metrics.model.ThroughputStats;
import io.confluent.kpay.payments.model.ConfirmedStats;
import io.confluent.kpay.payments.model.Payment;
import java.util.Collection;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes.StringSerde;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.apache.kafka.streams.state.StreamsMetadata;
import org.apache.kafka.streams.state.WindowStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Track min/max/avg payment throughput per second per day
 * Max payment and when
 * Note: Tap into the payments complete topic
 */
public class PaymentsThroughput {
    private long processedLast;
    long ONE_MINUTE = 60 * 1000L;

    private static final Logger log = LoggerFactory.getLogger(PaymentsThroughput.class);
    private final KTable<Windowed<String>, ThroughputStats> statsKTable;
    private ThroughputStats stats = new ThroughputStats();
    private ThroughputStats lastStats = new ThroughputStats();
    private final Topology topology;
    private Properties streamsConfig;
    private KafkaStreams streams;


    public PaymentsThroughput(String paymentsCompleteTopic, Properties streamsConfig){
        this.streamsConfig = streamsConfig;

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Payment> complete = builder.stream(paymentsCompleteTopic);

        /**
         * Data flow; emit the payments as Confirmed once they have been processed
         */
        Materialized<String, ThroughputStats, WindowStore<Bytes, byte[]>> completeStore = Materialized.as("throughput");
        Materialized<String, ThroughputStats, WindowStore<Bytes, byte[]>> completeWindowStore = completeStore.withKeySerde(new StringSerde()).withValueSerde(new ThroughputStats.Serde());

        /**
         * Statistic processing
         */
        statsKTable = complete
                .groupBy((key, value) -> "all-payments") // forces a repartition
                .windowedBy(TimeWindows.of(ONE_MINUTE))
                .aggregate(
                        ThroughputStats::new,
                        (key, value, aggregate) -> aggregate.update(value),
                        completeWindowStore
                );

        /**
         * Accumulate each window event internally by tracking the window as part of the aggregation
         */
        statsKTable.toStream().foreach((key, metricStats) -> {
            lastStats = metricStats;
                    if (processedLast != key.window().end()) {
                        stats = stats.merge(metricStats);
                    }
                    processedLast = key.window().end();
                });
        topology = builder.build();
    }
    public Topology getTopology() {
        return topology;
    }

    public void start() {
        streams = new KafkaStreams(topology, streamsConfig);
        streams.start();

        log.info(topology.describe().toString());
    }

    public void stop() {
        streams.close();
        streams.cleanUp();
    }

    public ReadOnlyWindowStore<String, ConfirmedStats> getStore() {
        return streams.store(statsKTable.queryableStoreName(), QueryableStoreTypes.windowStore());
    }

    /**
     * Expose statistics for rendering
     * @return
     */
    public ThroughputStats getStats() {
        return stats.merge(lastStats);
    }

    public Collection<StreamsMetadata> allMetaData() {
        return streams.allMetadata();
    }
}
