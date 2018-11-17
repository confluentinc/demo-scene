package io.confluent.demo.util;

import org.apache.kafka.common.serialization.Serde;

import java.util.Map;

public abstract class CountAndSumSerde<T1, T2> implements Serde<CountAndSum<T1, T2>> {

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {

  }

  @Override
  public void close() {

  }
}
