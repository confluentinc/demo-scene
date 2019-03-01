package io.confluent.kpay.payments.model;

import io.confluent.kpay.payments.AccountProcessor;
import io.confluent.kpay.util.JsonDeserializer;
import io.confluent.kpay.util.JsonSerializer;
import io.confluent.kpay.util.WrapperSerde;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class PaymentStats {
    private static final Logger log = LoggerFactory.getLogger(PaymentStats.class);

    private int count;
    private BigDecimal amount = new BigDecimal(0);

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentStats update(Payment value) {
        log.info(" InflightStats. update, processing:{} current:{} state:{}", value, this.amount, value.getState());

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
        return "PaymentStats{" +
                "count=" + count +
                ", amount=" + amount +
                '}';
    }

    static public final class Serde extends WrapperSerde<PaymentStats> {
        public Serde() {
            super(new JsonSerializer<>(), new JsonDeserializer(PaymentStats.class));
        }
    }
}
