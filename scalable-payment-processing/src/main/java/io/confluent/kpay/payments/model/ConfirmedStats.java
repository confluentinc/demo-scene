package io.confluent.kpay.payments.model;

import io.confluent.kpay.payments.PaymentsInFlight;
import io.confluent.kpay.util.JsonDeserializer;
import io.confluent.kpay.util.JsonSerializer;
import io.confluent.kpay.util.WrapperSerde;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfirmedStats {
    private static final Logger log = LoggerFactory.getLogger(PaymentsInFlight.class);

    private int count;
    private double amount;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public ConfirmedStats update(Payment value) {

        log.debug("handle:{}" + value);
    if (value.getState() == Payment.State.confirmed) {
        // remove 'complete'd payments
        this.amount += value.getAmount();
        this.count++;
    } else {
        // log error
    }
        return this;
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
