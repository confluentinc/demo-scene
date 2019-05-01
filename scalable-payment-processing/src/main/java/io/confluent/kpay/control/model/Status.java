package io.confluent.kpay.control.model;

import io.confluent.kpay.util.JsonDeserializer;
import io.confluent.kpay.util.JsonSerializer;
import io.confluent.kpay.util.WrapperSerde;

public class Status {

    private final int status;

    public Status(Code code) {
        this.status = code.ordinal();
    }
    public int getStatus() {
        return status;
    }

    public Code getCode() {
        return Code.values()[status];
    }

    public enum Code { START, STOP, PAUSE };



    static public final class Serde extends WrapperSerde<Status> {
        public Serde() {
            super(new JsonSerializer<>(), new JsonDeserializer<>(Status.class));
        }
    }

}
