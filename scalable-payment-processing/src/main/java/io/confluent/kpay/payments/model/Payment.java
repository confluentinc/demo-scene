package io.confluent.kpay.payments.model;

import io.confluent.kpay.util.JsonDeserializer;
import io.confluent.kpay.util.JsonSerializer;
import io.confluent.kpay.util.WrapperSerde;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.TransformerSupplier;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * States: incoming, debit, credit, complete, confirmed
 */
public class Payment {

    static Logger log = LoggerFactory.getLogger(Payment.class);

    public enum State {incoming, debit, credit, complete, confirmed};

    String id;
    String txnId;
    String from;
    String to;
    double amount;
    int state;

    public Payment(){};
    public Payment(String txnId, String id, String from, String to, double amount, State state){
        this.txnId = txnId;
        this.id = id;
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.state = state.ordinal();
    }

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public String getId() {
        return id;
    }

//    public void setId(String id) {
//        this.id = id;
//    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public State getState() {
        return State.values()[state];
    }

    /**
     * When changing state we need to rekey to the correct 'id' for debit and credit account processor instances so they are processed by the correct instance.
     * Upon completion need to rekey back to the txnId
     * @param state
     */
    public void setState(State state) {
        this.state = state.ordinal();
        if (state == State.credit) {
            id = to;
        } else if (state == State.debit) {
            id = from;
        } else {
            id = txnId;
        }
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id='" + id + '\'' +
                ", txnId='" + txnId + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", amount=" + amount +
                ", state=" + getState() +
                '}';
    }

    public void reset() {
        this.id = txnId;
    }

    public boolean isComplete() {
        return this.id.equalsIgnoreCase(this.txnId);
    }

    static public final class Serde extends WrapperSerde<Payment> {
        public Serde() {
            super(new JsonSerializer<>(), new JsonDeserializer<>(Payment.class));
        }
    }

    /**
     * Used to by InflightProcessor to either 1) change payment from 'incoming' --> 'debit' 2) ignore/filter 'complete' payments
     */
    static public class InflightTransformer implements TransformerSupplier<String, Payment, KeyValue<String, Payment>> {

        static Logger log = LoggerFactory.getLogger(InflightTransformer.class);

        @Override
        public org.apache.kafka.streams.kstream.Transformer<String, Payment, KeyValue<String, Payment>> get() {
            return new org.apache.kafka.streams.kstream.Transformer<String, Payment, KeyValue<String, Payment>>() {
                private ProcessorContext context;

                @Override
                public void init(ProcessorContext context) {
                    this.context = context;
                }

                @Override
                public KeyValue<String, Payment> transform(String key, Payment payment) {

                    log.info("transform 'incoming' to 'debit': {}", payment);

                    if (payment.getState() == State.incoming) {
                        payment.setState(State.debit);

                        // we have to rekey to the debit account so the 'debit' request is sent to the right AccountProcessor<accountId>
                        return new KeyValue<>(payment.getId(), payment);
                    } else if (payment.getState() == State.complete) {
                        return null;
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
