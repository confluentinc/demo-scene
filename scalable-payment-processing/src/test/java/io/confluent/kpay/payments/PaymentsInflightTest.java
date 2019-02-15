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
import io.confluent.kpay.payments.model.Payment;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.processor.StateStore;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Properties;

public class PaymentsInflightTest {


  private TopologyTestDriver testDriver;

  @Before
  public void before() throws Exception {


  }

  @After
  public void after() {
  }


  @Test
  public void incomingAndCompletePaymentLeaveBalanceAtZero() throws Exception {


    String incoming = "payments.incoming";
    String inflight = "payments.inflight";
    String complete = "payments.complete";

    PaymentsInFlight paymentsInFlight = new PaymentsInFlight(incoming, inflight, complete, getProperties("localhost:9091"), new PauseControllable());
    paymentsInFlight.start();

    Topology topology = paymentsInFlight.getTopology();

    Properties streamsConfig = getProperties("localhost:9091");

    testDriver = new TopologyTestDriver(topology, streamsConfig);

    // setup
    ConsumerRecordFactory<String, Payment> factory = new ConsumerRecordFactory<>(
            complete,
            new StringSerializer(),
            new Payment.Serde().serializer()
    );

    // test
    Payment payment = new Payment("txnId", "id","from","to", 123.0, Payment.State.complete);
    testDriver.pipeInput(factory.create(complete, "key", payment));
    testDriver.close();


    // verify
    ProducerRecord<String, Payment> inflightRecord =  testDriver.readOutput(
            inflight,
            new StringDeserializer(),
            new Payment.Serde().deserializer()
    );

    Assert.assertNull(inflightRecord);

    Map<String, StateStore> allStateStores = testDriver.getAllStateStores();
    for (Map.Entry<String, StateStore> stringStateStoreEntry : allStateStores.entrySet()) {
      System.out.println(stringStateStoreEntry.getKey() + ":" + stringStateStoreEntry.getValue());
    }
  }



  @Test
  public void completePaymentEmitsNothing() throws Exception {


    String incoming = "payments.incoming";
    String inflight = "payments.inflight";
    String complete = "payments.complete";

    PaymentsInFlight paymentsInFlight = new PaymentsInFlight(incoming, inflight, complete, getProperties("localhost:9091"), new PauseControllable());
    paymentsInFlight.start();

    Topology topology = paymentsInFlight.getTopology();

    Properties streamsConfig = getProperties("localhost:9091");

    testDriver = new TopologyTestDriver(topology, streamsConfig);

    // setup
    ConsumerRecordFactory<String, Payment> factory = new ConsumerRecordFactory<>(
            complete,
            new StringSerializer(),
            new Payment.Serde().serializer()
    );

    // test
    Payment payment = new Payment("txnId", "id","from","to", 123.0, Payment.State.complete);
    testDriver.pipeInput(factory.create(complete, "key", payment));
    testDriver.close();


    // verify
    ProducerRecord<String, Payment> inflightRecord =  testDriver.readOutput(
            inflight,
            new StringDeserializer(),
            new Payment.Serde().deserializer()
    );

    Assert.assertNull(inflightRecord);

    Map<String, StateStore> allStateStores = testDriver.getAllStateStores();
    for (Map.Entry<String, StateStore> stringStateStoreEntry : allStateStores.entrySet()) {
      System.out.println(stringStateStoreEntry.getKey() + ":" + stringStateStoreEntry.getValue());
    }
  }


  @Test
  public void incomingPaymentEmitsDebitPayment() throws Exception {

    String incoming = "payments.incoming";
    String inflight = "payments.inflight";
    String complete = "payments.complete";

    PaymentsInFlight paymentsInFlight = new PaymentsInFlight(incoming, inflight, complete, getProperties("localhost:9091"), new PauseControllable());
    paymentsInFlight.start();

    Topology topology = paymentsInFlight.getTopology();

    Properties streamsConfig = getProperties("localhost:9091");

    testDriver = new TopologyTestDriver(topology, streamsConfig);

    // setup
    ConsumerRecordFactory<String, Payment> factory = new ConsumerRecordFactory<>(
            incoming,
            new StringSerializer(),
            new Payment.Serde().serializer()
    );

    // test
    Payment payment = new Payment("txnId", "id","from","to", 123.0, Payment.State.incoming);
    testDriver.pipeInput(factory.create(incoming, "key", payment));
    testDriver.close();


    // verify
    ProducerRecord<String, Payment> inflightRecord =  testDriver.readOutput(
            inflight,
            new StringDeserializer(),
            new Payment.Serde().deserializer()
    );

    Assert.assertNotNull(inflightRecord);

    System.out.println("Inflight Records:" + inflightRecord.value());
  }

  private Properties getProperties(String broker) {
    Properties props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "TEST-APP-ID");// + System.currentTimeMillis());
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Payment.Serde.class.getName());
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 2000);
    return props;
  }

}
