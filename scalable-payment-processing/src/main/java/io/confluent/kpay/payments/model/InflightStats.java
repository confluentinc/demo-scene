package io.confluent.kpay.payments.model;

import io.confluent.kpay.util.JsonDeserializer;
import io.confluent.kpay.util.JsonSerializer;
import io.confluent.kpay.util.WrapperSerde;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InflightStats {
    private static final Logger log = LoggerFactory.getLogger(InflightStats.class);

    private int count;
    private BigDecimal amount = new BigDecimal(0);
    private long timestamp;

    public InflightStats update(Payment value) {
        log.debug(" InflightStats. update, processing:{} current:{} state:{}", value, this.amount, value.getState());

        this.timestamp = System.currentTimeMillis();
        if (value.getState() == Payment.State.incoming) {
            // accumulate on 'incoming' payment
            this.amount = this.amount.add(value.getAmount());
            this.count++;
        } else if (value.getState() == Payment.State.complete) {
            // remove 'complete'd payments
            this.amount = this.amount.subtract(value.getAmount());
            this.count--;
        }
        return this;
    }

    @Override
    public String toString() {
        return "InflightStats{" +
                "count=" + count +
                ", amount=" + amount +
                '}';
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void add(InflightStats other) {
        this.amount.add(other.amount);
        this.count += other.count;
    }


    static public final class Serde extends WrapperSerde<InflightStats> {
        public Serde() {
            super(new JsonSerializer<>(), new JsonDeserializer(InflightStats.class));
        }
    }
}
