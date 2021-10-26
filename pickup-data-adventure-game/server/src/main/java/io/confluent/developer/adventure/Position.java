package io.confluent.developer.adventure;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Position {
  @JsonProperty("X")
  public Integer x;
  @JsonProperty("Y")
  public Integer y;

  public Integer getX() {
    return x;
  }

  public void setX(Integer x) {
    this.x = x;
  }

  public Integer getY() {
    return y;
  }

  public void setY(Integer y) {
    this.y = y;
  }

  public Position() {
  }

  public Position(Integer x, Integer y) {
    this.x = x;
    this.y = y;
  }

  public String toString() {
    return new ToStringBuilder(this, SHORT_PREFIX_STYLE).append("x", x).append("y", y).toString();
  }

  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }
}
