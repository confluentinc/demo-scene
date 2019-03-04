package io.confluent.kpay.payments.model;

import io.confluent.kpay.payments.PaymentsInFlight;
import io.confluent.kpay.util.JsonDeserializer;
import io.confluent.kpay.util.JsonSerializer;
import io.confluent.kpay.util.WrapperSerde;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class ConfirmedStats {
    private static final Logger log = LoggerFactory.getLogger(PaymentsInFlight.class);

    private int count;
    private BigDecimal amount = new BigDecimal(0);
    private long timestamp;

    public ConfirmedStats update(Payment value) {

        this.timestamp = System.currentTimeMillis();
        log.debug("handle:{}" + value);
        if (value.getState() == Payment.State.confirmed) {
            // remove 'complete'd payments
            this.amount = this.amount.add(value.getAmount());
            this.count++;
        } else {
            // log error
        }
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    @Override
    public String toString() {
        return "ConfirmedStats{" +
                "count=" + count +
                ", amount=" + amount +
                '}';
    }

    static public final class Serde extends WrapperSerde<ConfirmedStats> {
        public Serde() {
            super(new JsonSerializer<>(), new JsonDeserializer(ConfirmedStats.class));
        }
    }
}
