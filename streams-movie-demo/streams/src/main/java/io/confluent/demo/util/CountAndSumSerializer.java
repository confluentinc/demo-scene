package io.confluent.demo.util;

import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

public class CountAndSumSerializer<T1,T2> implements Serializer<CountAndSum<T1, T2>> {

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
  }

  @Override
  public byte[] serialize(String topic, CountAndSum<T1, T2> data) {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    ObjectOutputStream out = null;
    byte[] bytes = null;
    try {
      out = new ObjectOutputStream(stream);
      out.writeObject(data.count);
      out.writeObject(data.sum);
      bytes = stream.toByteArray();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return bytes;
  }

  @Override
  public void close() {

  }
}
