/**
 * Copyright 2018 Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package io.confluent.kpay.payments;

import io.confluent.kpay.control.PauseControllable;
import io.confluent.kpay.payments.model.AccountBalance;
import io.confluent.kpay.payments.model.ConfirmedStats;
import io.confluent.kpay.payments.model.Payment;
import io.confluent.kpay.payments.model.PaymentStats;
import io.confluent.kpay.utils.IntegrationTestHarness;
import org.apache.commons.collections.map.HashedMap;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.apache.kafka.streams.state.StreamsMetadata;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

public class RemoteIntegrationTest {


  private static String paymentsCompleteTopic = "payments." + Payment.State.complete;
  private static String paymentsInflightTopic = "payments.inflight"; // debit and credit processing
  private long instanceId = System.currentTimeMillis();


  @Before
  public void before() throws Exception {

  }

  @After
  public void after() {
  }

  @Test
  public void runAnotherServiceThingy() throws Exception {


    AccountProcessor accountProcessor = new AccountProcessor(paymentsInflightTopic, paymentsCompleteTopic, getProperties("127.0.0.1:63602"));
    accountProcessor.start();


    Thread.sleep(100);


    System.out.println("METAAAAAAAA :" + accountProcessor.allMetaData());;

  }

  private Properties getProperties(String broker) {
    Properties props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "TEST-APP-ID-" + instanceId++);
    props.put(StreamsConfig.APPLICATION_SERVER_CONFIG, "localhost:22222");
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Payment.Serde.class.getName());
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 2000);
    return props;
  }

}
