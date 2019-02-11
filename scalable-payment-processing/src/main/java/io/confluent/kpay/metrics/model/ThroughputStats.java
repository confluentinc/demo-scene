package io.confluent.kpay.metrics.model;

import io.confluent.kpay.payments.PaymentsInFlight;
import io.confluent.kpay.payments.model.Payment;
import io.confluent.kpay.util.JsonDeserializer;
import io.confluent.kpay.util.JsonSerializer;
import io.confluent.kpay.util.WrapperSerde;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks per second metrics
 * - totalPayments number of events per second
 * - totalPayments dollar amount
 * - latency min/max/avg (getElapsedMillis)
 */
public class ThroughputStats {
    private static final Logger log = LoggerFactory.getLogger(PaymentsInFlight.class);

    private int totalPayments;
    private int throughputPerWindow;
    private double totalDollarAmount;
    private long minLatency = Long.MAX_VALUE;
    private long maxLatency;
    private Payment largestPayment;

    public ThroughputStats update(Payment payment) {
        totalPayments++;
        totalDollarAmount += payment.getAmount();
        maxLatency = Math.max(maxLatency, payment.getElapsedMillis());
        minLatency = Math.min(minLatency, payment.getElapsedMillis());
        if (largestPayment == null || payment.getAmount() > largestPayment.getAmount()) {
            largestPayment = payment;
        }
        return this;
    }

    public ThroughputStats merge(ThroughputStats otherStats) {
        ThroughputStats result = new ThroughputStats();
        result.throughputPerWindow =  Math.max(this.throughputPerWindow, otherStats.throughputPerWindow);
        result.totalPayments += otherStats.totalPayments;
        result.largestPayment = this.largestPayment != null && this.largestPayment.getAmount() > otherStats.largestPayment.getAmount() ? this.largestPayment : otherStats.largestPayment;
        result.totalDollarAmount += otherStats.totalDollarAmount;
        result.minLatency = Math.min(this.minLatency, otherStats.minLatency);
        result.maxLatency = Math.max(this.maxLatency, otherStats.maxLatency);
        return result;
    }

    public int getTotalPayments() {
        return totalPayments;
    }

    public int getThroughputPerWindow() {
        return throughputPerWindow;
    }

    public double getTotalDollarAmount() {
        return totalDollarAmount;
    }

    public long getMinLatency() {
        return minLatency;
    }

    public long getMaxLatency() {
        return maxLatency;
    }

    public Payment getLargestPayment() {
        return largestPayment;
    }

    @Override
    public String toString() {
        return "ThroughputStats{" +
                "totalPayments=" + totalPayments +
                ", throughputPerWindow=" + throughputPerWindow +
                ", totalDollarAmount=" + totalDollarAmount +
                ", minLatency=" + minLatency +
                ", maxLatency=" + maxLatency +
                ", largestPayment=" + largestPayment +
                '}';
    }

    static public final class Serde extends WrapperSerde<ThroughputStats> {
        public Serde() {
            super(new JsonSerializer<>(), new JsonDeserializer(ThroughputStats.class));
        }
    }
}
