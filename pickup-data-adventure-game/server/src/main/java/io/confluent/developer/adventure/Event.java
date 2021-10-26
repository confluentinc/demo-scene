package io.confluent.developer.adventure;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Event<K, V> {
  private String source;
  private K key;
  private V value;

  public Event() {
  }

  public Event(String source, K key, V value) {
    this.source = source;
    this.key = key;
    this.value = value;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public K getKey() {
    return this.key;
  }

  public void setKey(K key) {
    this.key = key;
  }

  public V getValue() {
    return this.value;
  }

  public void setValue(V value) {
    this.value = value;
  }

  public String toString() {
    return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
      .append("source", source)
      .append("key", key)
      .append("value", value)
      .toString();
  }
}
