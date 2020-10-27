package io.confluent.developer.ccloud.demo.kstream.domain;

import lombok.Value;

@Value
public class TransactionResult {

  public enum ErrorType {
    INSUFFICIENT_FUNDS
  }

  Transaction transaction;
  Funds funds;
  boolean success;
  ErrorType errorType;
}
