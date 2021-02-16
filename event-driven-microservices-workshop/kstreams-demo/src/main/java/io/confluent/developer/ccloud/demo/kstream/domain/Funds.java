package io.confluent.developer.ccloud.demo.kstream.domain;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Funds {

  String account;
  BigDecimal balance;

}
