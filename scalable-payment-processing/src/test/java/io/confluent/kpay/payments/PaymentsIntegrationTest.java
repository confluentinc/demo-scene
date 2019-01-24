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

import io.confluent.kpay.payments.model.AccountBalance;
import io.confluent.kpay.payments.model.Payment;
import io.confluent.kpay.payments.model.PaymentStats;
import io.confluent.kpay.utils.IntegrationTestHarness;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

public class PaymentsIntegrationTest {


  private TopologyTestDriver testDriver;
  private IntegrationTestHarness testHarness;
  private String bootstrapServers;

  private static String paymentsIncomingTopic = "payments." + Payment.State.incoming;
  private static String paymentsCompleteTopic = "payments." + Payment.State.complete;
  private static String paymentsConfirmedTopic = "payments." + Payment.State.confirmed;
  private static String paymentsInflightTopic = "payments.inflight"; // debit and credit processing
  private long instanceId = System.currentTimeMillis();


  @Before
  public void before() throws Exception {

    testHarness = new IntegrationTestHarness();
    testHarness.start();
    bootstrapServers = testHarness.embeddedKafkaCluster.bootstrapServers();
    System.setProperty("bootstrap.servers", bootstrapServers);

    testHarness.createTopic(paymentsInflightTopic, 1, 1);
    testHarness.createTopic(paymentsIncomingTopic, 1, 1);
    testHarness.createTopic(paymentsCompleteTopic, 1, 1);
  }

  @After
  public void after() {
    testHarness.stop();
  }

  @Test
  public void serviceSinglePayment() throws Exception {

    PaymentsInFlight paymentsInFlight = new PaymentsInFlight(paymentsIncomingTopic, paymentsInflightTopic, paymentsCompleteTopic, getProperties(bootstrapServers));
    paymentsInFlight.start();

    AccountProcessor accountProcessor = new AccountProcessor(paymentsInflightTopic, paymentsCompleteTopic, getProperties(bootstrapServers));
    accountProcessor.start();

    PaymentsConfirmed paymentsConfirmed = new PaymentsConfirmed(paymentsCompleteTopic, paymentsConfirmedTopic, getProperties(bootstrapServers));
    paymentsConfirmed.start();


    Map<String, Payment> records = Collections.singletonMap("record1", new Payment("tnxId", "record1", "neil", "john", 10, Payment.State.incoming));
    testHarness.produceData(paymentsIncomingTopic, records, new Payment.Serde().serializer(), System.currentTimeMillis());


    // if the payment flowed through all 4 processing stages (2x account processors) - then we expect a paymentsConfirmed event
    Map<String, Payment> confirmed = testHarness.consumeData(paymentsConfirmedTopic, 1, new StringDeserializer(), new Payment.Serde().deserializer(), 1000);


    Assert.assertNotNull(confirmed);
    Assert.assertEquals(Payment.State.confirmed, confirmed.values().iterator().next().getState());


    System.out.println("Test Confirmed:"+ confirmed);

    // verify the postal state of each processor, i.e. inflight == 0; neil == -10; john == 10; confirmed == 10

    KeyValueIterator<Windowed<String>, PaymentStats> all = paymentsInFlight.getStore().all();

    PaymentStats value = all.next().value;

    // should be 0
    System.out.println("Test Inflight:" + value);


    KeyValueIterator<String, AccountBalance> all1 = accountProcessor.getStore().all();
    while (all1.hasNext()) {
      System.out.println("Test account:" + all1.next().value);
    }

    System.out.println();


  }


//  @Test
//  public void serviceMultiplePayments() throws Exception {
//
//    PaymentsInFlight paymentsInFlight = new PaymentsInFlight(paymentsIncoming, paymentsinflight, getProperties(bootstrapServers));
//    paymentsInFlight.start();
//
//    Map<String, Payment> records = new HashMap<>();//
//    records.put("1", new Payment("id", "1", "neil", "john", 10, Payment.State.incoming));
//    records.put("2", new Payment("id", "1", "neil", "john", 100, Payment.State.incoming));
//    records.put("3", new Payment("id", "1", "neil", "john", 1000, Payment.State.incoming));
//
//    testHarness.produceData(paymentsIncoming, records, new Payment.Serde().serializer(), System.currentTimeMillis());
//
//    Map<String, Payment> inflightPayment = testHarness.consumeData(paymentsinflight, 1, new StringDeserializer(), new Payment.Serde().deserializer(), 1000);
//
//    Assert.assertTrue(inflightPayment.size() > 0);
//
//    System.out.println("Inflight:"+ inflightPayment);
//
//    KeyValueIterator<Windowed<String>, PaymentStats> all = paymentsInFlight.getStore().all();
//    Object value = all.next().value;
//    System.out.println(value);
//  }



  private Properties getProperties(String broker) {
    Properties props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "TEST-APP-ID-" + instanceId++);
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Payment.Serde.class.getName());
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 2000);
    return props;
  }

}
