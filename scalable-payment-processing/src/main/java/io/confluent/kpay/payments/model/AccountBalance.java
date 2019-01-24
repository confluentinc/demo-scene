package io.confluent.kpay.payments.model;

import io.confluent.kpay.util.JsonDeserializer;
import io.confluent.kpay.util.JsonSerializer;
import io.confluent.kpay.util.WrapperSerde;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.kstream.TransformerSupplier;
import org.apache.kafka.streams.processor.ProcessorContext;

/**
 * TODO: need validation mechanism to ensure accounts were created correctly = i.e. prevent adhoc account creation
 *
 * TODO: check there is enough money in the account
 */
public class AccountBalance {


    private String name;
    private Payment lastPayment;
    private double amount;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        System.out.println("amount:" + amount);
        this.amount = amount;
    }

    public AccountBalance handle(String key, Payment value) {

        this.name = value.id;

        if (value.getState() == Payment.State.debit) {
            this.amount -= value.amount;
        } else if (value.getState() == Payment.State.credit) {
            this.amount += value.amount;
        } else {
            // report to dead letter queue via exception handler
            throw new RuntimeException("Invalid payment received:" + value);
        }
        this.lastPayment = value;
        return this;
    }


    public Payment getLastPayment() {
        return lastPayment;
    }


    @Override
    public String toString() {
        return "AccountBalance{" +
                "name='" + name + '\'' +
                ", amount=" + amount +
                '}';
    }

    static public final class Serde extends WrapperSerde<AccountBalance> {
        public Serde() {
            super(new JsonSerializer<>(), new JsonDeserializer(AccountBalance.class));
        }
    }

    /**
     * use the Flip to 1) when we receive a debit event flip to a credit 2) when we get a credit event - break the circuit and dont emit anything
     */

    static public class FlipTransformerSupplier implements TransformerSupplier<String, AccountBalance, KeyValue<String, Payment>>  {
        @Override
        public Transformer<String, AccountBalance, KeyValue<String, Payment>> get() {
            return new Transformer<String, AccountBalance, KeyValue<String, Payment>>() {
                private ProcessorContext context;

                @Override
                public void init(ProcessorContext context) {
                    this.context = context;
                }

                @Override
                public KeyValue<String, Payment> transform(String key, AccountBalance value) {
                    Payment payment = value.lastPayment;
                    if (payment.getState() == Payment.State.debit) {
                        // we have to rekey to the debit account so the 'debit' request is sent to the right AccountProcessor<accountId>
                        payment.setState(Payment.State.credit);
                        return new KeyValue<>(payment.getId(), payment);
                    } else  if (payment.getState() == Payment.State.credit) {
                        // already processed the credit emit to payment complete with
                        payment.setState(Payment.State.complete);
                        return new KeyValue<>(payment.getId(), payment);
                    } else {
                        // exception handler will report to DLQ
                        throw new RuntimeException("Invalid Payment state, expecting debit or credit but got" + payment.getState() + ": "+ payment.toString());
                    }
                }

                @Override
                public void close() {
                }
            };
        }
    };
}
