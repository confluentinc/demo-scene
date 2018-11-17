package io.confluent.demo.util;

import org.apache.kafka.common.serialization.Deserializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

public class CountAndSumDeserializer<T1, T2> implements Deserializer<CountAndSum<T1, T2>> {

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
  }

  @Override
  public CountAndSum<T1, T2> deserialize(String topic, byte[] data) {
    if (data == null || data.length == 0) {
      return null;
    }
    ByteArrayInputStream stream = new ByteArrayInputStream(data);
    T1 sum = null;
    T2 count = null;
    try {
      ObjectInputStream ois = new ObjectInputStream(stream);
      sum = (T1) ois.readObject();
      count = (T2) ois.readObject();
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException("Unable to deserialize CountAndSum obj", e);
    }
    return new CountAndSum<>(sum, count);
  }

  @Override
  public void close() {

  }
}
