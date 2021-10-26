/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.confluent.developer.adventure;

import java.util.Map;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonPOJODeserializer<T> implements Deserializer<T> {
  private static Logger logger = LoggerFactory.getLogger(JsonPOJODeserializer.class);
  private ObjectMapper objectMapper;

  private Class<T> tClass;

  public JsonPOJODeserializer() {
    objectMapper = new ObjectMapper();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void configure(Map<String, ?> props, boolean isKey) {
    tClass = (Class<T>) props.get("JsonPOJOClass");
  }

  @Override
  public T deserialize(String topic, byte[] bytes) {
    if (bytes == null) {
      return null;
    }

    try {
      T data = objectMapper.readValue(bytes, tClass);
      logger.debug("Deserializing {} {}", topic, data);
      return data;
    } catch (Exception e) {
      throw new SerializationException(e);
    }
  }

  @Override
  public void close() {
  }
}
