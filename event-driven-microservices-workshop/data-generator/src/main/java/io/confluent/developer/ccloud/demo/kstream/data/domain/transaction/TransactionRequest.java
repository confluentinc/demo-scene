package io.confluent.developer.ccloud.demo.kstream.data.domain.transaction;

import java.math.BigDecimal;

import io.confluent.developer.ccloud.demo.kstream.data.domain.transaction.Transaction.Type;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

  String account;
  BigDecimal amount;
  Type type;
  String currency;
  String country;
}
