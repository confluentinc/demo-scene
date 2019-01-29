package io.confluent.kpay.payments.model;

import io.confluent.kpay.payments.AccountProcessor;
import io.confluent.kpay.util.JsonDeserializer;
import io.confluent.kpay.util.JsonSerializer;
import io.confluent.kpay.util.WrapperSerde;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentStats {
    private static final Logger log = LoggerFactory.getLogger(PaymentStats.class);

    private int count;
    private double amount;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public PaymentStats update(Payment value) {
                log.info(" InflightStats. update, processing:{} current:{} state:{}", value, this.amount, value.getState());

        /**
         * Note: the transformer will intercept the message and convert it to from 'incoming' -> 'debit' OR 'complete'
         * The 'debit
         * We need to process the debit
         */
        if (value.getState() == Payment.State.debit) {
            // accumulate on 'incoming' payment
            this.amount += value.amount;
            this.count++;
        } else if (value.getState() == Payment.State.complete) {
            // remove 'complete'd payments
            this.amount -= value.amount;
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
