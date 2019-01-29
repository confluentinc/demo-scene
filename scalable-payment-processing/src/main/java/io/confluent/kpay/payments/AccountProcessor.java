package io.confluent.kpay.payments;

import io.confluent.kpay.payments.model.AccountBalance;
import io.confluent.kpay.payments.model.Payment;
import org.apache.kafka.common.serialization.Serdes.StringSerde;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class AccountProcessor {
    private final KTable<String, AccountBalance> accountBalanceKTable;
    private final Topology topology;

    private static final Logger log = LoggerFactory.getLogger(AccountProcessor.class);

    private Properties streamsConfig;
    private KafkaStreams streams;


    public AccountProcessor(String paymentsInflightTopic, String paymentsCompleteTopic, Properties streamsConfig){
        this.streamsConfig = streamsConfig;

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Payment> inflight = builder.stream(paymentsInflightTopic);

        Materialized<String, AccountBalance, KeyValueStore<Bytes, byte[]>> account = Materialized.as("account");
        Materialized<String, AccountBalance, KeyValueStore<Bytes, byte[]>> accountStore = account.withKeySerde(new StringSerde()).withValueSerde(new AccountBalance.Serde());



        // debit & credit processing topology
        accountBalanceKTable = inflight.groupByKey()
                .aggregate(
                        AccountBalance::new,
                        (key, value, aggregate) -> aggregate.handle(key, value),
                        accountStore
                );

        Predicate<String, Payment> isCreditRecord =  (key, value) -> value.getState() == Payment.State.credit;
        Predicate<String, Payment> isCompleteRecord =  (key, value) -> value.getState() == Payment.State.complete;

        // handle payment state
        KStream<String, Payment>[] branch = inflight
                .map((KeyValueMapper<String, Payment, KeyValue<String, Payment>>) (key, value) -> {
                    if (value.getState() == Payment.State.debit) {
                        value.setState(Payment.State.credit);
                    } else if (value.getState() == Payment.State.credit) {
                        value.setState(Payment.State.complete);
                    } else if (value.getState() == Payment.State.complete) {
                        log.error("Invalid payment:{}", value);
                        throw new RuntimeException("Invalid payment state:" + value);
                    }
                    return new KeyValue<>(value.getId(), value);
                })
                .branch(isCreditRecord, isCompleteRecord);


        branch[0].to(paymentsInflightTopic);
        branch[1].to(paymentsCompleteTopic);



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

    public ReadOnlyKeyValueStore<String, AccountBalance> getStore() {
        return streams.store(accountBalanceKTable.queryableStoreName(), QueryableStoreTypes.keyValueStore());
    }
}
