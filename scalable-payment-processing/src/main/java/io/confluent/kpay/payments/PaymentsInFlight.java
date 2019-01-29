package io.confluent.kpay.payments;

import io.confluent.kpay.payments.model.Payment;
import io.confluent.kpay.payments.model.PaymentStats;
import org.apache.kafka.common.serialization.Serdes.StringSerde;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.apache.kafka.streams.state.WindowStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Properties;

public class PaymentsInFlight {
    private static final Logger log = LoggerFactory.getLogger(PaymentsInFlight.class);

    private static long ONE_DAY = 24 * 60 * 60 * 1000L;


    private final KTable<Windowed<String>, PaymentStats> paymentStatsKTable;
    private final Topology topology;
    private Properties streamsConfig;
    private KafkaStreams streams;


    public PaymentsInFlight(String paymentsIncomingTopic, String paymentsInflightTopic, String paymentsCompleteTopic, Properties streamsConfig){
        this.streamsConfig = streamsConfig;

        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, Payment> inflight = builder.stream(Arrays.asList(paymentsIncomingTopic, paymentsCompleteTopic));

        // emit the  payments as Debits on the 'inflight' stream
        inflight.transform(new Payment.InflightTransformer()).to(paymentsInflightTopic);

        Materialized<String, PaymentStats, WindowStore<Bytes, byte[]>> inflightFirst = Materialized.as("inflight");
        Materialized<String, PaymentStats, WindowStore<Bytes, byte[]>> inflightWindowStore = inflightFirst.withKeySerde(new StringSerde()).withValueSerde(new PaymentStats.Serde());

        paymentStatsKTable = inflight
                .groupBy((key, value) -> "all-payments") // will force a repartition-topic :(
                .windowedBy(TimeWindows.of(ONE_DAY))
                .aggregate(
                        PaymentStats::new,
                        (key, value, aggregate) -> aggregate.update(value),
                        inflightWindowStore
                );

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

    public ReadOnlyWindowStore<String, PaymentStats> getStore() {
        return streams.store(paymentStatsKTable.queryableStoreName(), QueryableStoreTypes.windowStore());
    }
}
