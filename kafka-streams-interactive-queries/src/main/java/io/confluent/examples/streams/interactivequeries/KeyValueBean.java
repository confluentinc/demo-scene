/*
 * Copyright Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.confluent.examples.streams.interactivequeries;

import java.util.Objects;

/**
 * A simple bean used by {@link WordCountInteractiveQueriesRestService} when responding to
 * {@link WordCountInteractiveQueriesRestService#byKey(String, String)}.
 *
 * We use this JavaBean based approach as it fits nicely with JSON serialization provided by
 * jax-rs/jersey
 */
public class KeyValueBean {

  private String key;
  private Long value;

  public KeyValueBean() {}

  public KeyValueBean(final String key, final Long value) {
    this.key = key;
    this.value = value;
  }

  public String getKey() {

    return key;
  }

  public void setKey(final String key) {
    this.key = key;
  }

  public Long getValue() {
    return value;
  }

  public void setValue(final Long value) {
    this.value = value;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final KeyValueBean that = (KeyValueBean) o;
    return Objects.equals(key, that.key) &&
           Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, value);
  }

  @Override
  public String toString() {
    return "KeyValueBean{" +
           "key='" + key + '\'' +
           ", value=" + value +
           '}';
  }
}
