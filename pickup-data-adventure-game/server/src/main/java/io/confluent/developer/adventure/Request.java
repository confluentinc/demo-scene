package io.confluent.developer.adventure;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Request {
  private UUID userId;
  private String command;

  public Request() {
  }

  public Request(UUID userId, String command) {
    this.userId = userId;
    this.command = command;
  }

  public UUID getUserId() {
    return this.userId;
  }

  public String getCommand() {
    return this.command;
  }

  public String toString() {
    return new ToStringBuilder(this, SHORT_PREFIX_STYLE).append("userId", userId).append("command", command).toString();
  }
}
