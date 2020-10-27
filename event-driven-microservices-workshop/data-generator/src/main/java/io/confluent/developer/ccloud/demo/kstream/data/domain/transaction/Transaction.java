package io.confluent.developer.ccloud.demo.kstream.data.domain.transaction;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Transaction {

  public enum Type {
    DEPOSIT, WITHDRAW
  }

  private final String guid;
  private final String account;
  private final BigDecimal amount;
  private final Type type;
  private final String currency;
  private final String country;
  
}
