package io.confluent.kpay.metrics.model;

import io.confluent.kpay.payments.PaymentsInFlight;
import io.confluent.kpay.payments.model.Payment;
import io.confluent.kpay.util.JsonDeserializer;
import io.confluent.kpay.util.JsonSerializer;
import io.confluent.kpay.util.WrapperSerde;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks aggregate view of per second instances of ThroughPutStatsPerSecond
 */
public class ThroughputStatsAggregate {
    private static final Logger log = LoggerFactory.getLogger(PaymentsInFlight.class);

    ThroughputStats stats;

    public ThroughputStatsAggregate update(ThroughputStats value) {
        log.debug("handle:{}" + value);
        if (stats == null) {
            stats = value;
        }

        stats.merge(value);
        return this;
    }

    static public final class Serde extends WrapperSerde<ThroughputStatsAggregate> {
        public Serde() {
            super(new JsonSerializer<>(), new JsonDeserializer(ThroughputStatsAggregate.class));
        }
    }
}
