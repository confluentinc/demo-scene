package io.confluent.kpay.metrics.model;

import io.confluent.kpay.payments.PaymentsInFlight;
import io.confluent.kpay.payments.model.Payment;
import io.confluent.kpay.util.JsonDeserializer;
import io.confluent.kpay.util.JsonSerializer;
import io.confluent.kpay.util.WrapperSerde;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

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
    private BigDecimal totalDollarAmount = new BigDecimal(0);
    private long minLatency = Long.MAX_VALUE;
    private long maxLatency;
    private Payment largestPayment;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    private long timestamp;

    public ThroughputStats update(Payment payment) {
        totalPayments++;
        totalDollarAmount = totalDollarAmount.add(payment.getAmount());
        maxLatency = Math.max(maxLatency, payment.getElapsedMillis());
        minLatency = Math.min(minLatency, payment.getElapsedMillis());
        if (largestPayment == null || payment.getAmount().doubleValue() > largestPayment.getAmount().doubleValue()) {
            largestPayment = payment;
        }
        timestamp = System.currentTimeMillis();
        return this;
    }

    public ThroughputStats merge(ThroughputStats otherStats) {
        ThroughputStats result = new ThroughputStats();
        result.throughputPerWindow =  Math.max(this.throughputPerWindow, otherStats.throughputPerWindow);
        result.totalPayments += otherStats.totalPayments;
        result.largestPayment = this.largestPayment != null && this.largestPayment.getAmount().doubleValue() > otherStats.largestPayment.getAmount().doubleValue() ? this.largestPayment : otherStats.largestPayment;
        result.totalDollarAmount = result.totalDollarAmount.add(otherStats.totalDollarAmount);
        result.minLatency = Math.min(this.minLatency, otherStats.minLatency);
        result.maxLatency = Math.max(this.maxLatency, otherStats.maxLatency);
        result.timestamp = System.currentTimeMillis();
        return result;
    }


    @Override
    public String toString() {
        return "ThroughputStats{" +
                "totalPayments=" + totalPayments +
                ", throughputPerWindow=" + throughputPerWindow +
                ", totalDollarAmount=" + totalDollarAmount.doubleValue() +
                ", minLatency=" + minLatency +
                ", maxLatency=" + maxLatency +
                ", largestPayment=" + largestPayment +
                '}';
    }

    public void setTotalPayments(int totalPayments) {
        this.totalPayments = totalPayments;
    }

    public void setThroughputPerWindow(int throughputPerWindow) {
        this.throughputPerWindow = throughputPerWindow;
    }

    public void setTotalDollarAmount(BigDecimal totalDollarAmount) {
        this.totalDollarAmount = totalDollarAmount;
    }

    public void setMinLatency(long minLatency) {
        this.minLatency = minLatency;
    }

    public void setMaxLatency(long maxLatency) {
        this.maxLatency = maxLatency;
    }

    public void setLargestPayment(Payment largestPayment) {
        this.largestPayment = largestPayment;
    }

    public int getTotalPayments() {
        return totalPayments;
    }

    public int getThroughputPerWindow() {
        return throughputPerWindow;
    }

    public BigDecimal getTotalDollarAmount() {
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

    static public final class Serde extends WrapperSerde<ThroughputStats> {
        public Serde() {
            super(new JsonSerializer<>(), new JsonDeserializer(ThroughputStats.class));
        }
    }
}
