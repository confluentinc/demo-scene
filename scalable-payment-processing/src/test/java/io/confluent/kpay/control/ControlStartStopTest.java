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
package io.confluent.kpay.control;

import io.confluent.kpay.payments.model.Payment;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ControlStartStopTest {


  private TopologyTestDriver testDriver;
  private volatile boolean waiting;
  private int counted;

  @Before
  public void before() throws Exception {


  }

  @After
  public void after() {
  }



  @Test
  public void shouldStartStopStartAfterDuration() throws Exception {




    StreamsBuilder builder = new StreamsBuilder();
    String topic = "test-topic";
    KStream<String, Payment> stream = builder.stream(topic);

    Predicate<? super String, ? super Payment> isRunning = (key, value) -> isProcessingRunning();
    stream.filter(isRunning).mapValues(value -> {
      log("COUNTING!");
      counted++;
      return null;
    });


    Topology topology = builder.build();

    Properties streamsConfig = getProperties("localhost:9091");

    testDriver = new TopologyTestDriver(topology, streamsConfig);

    // setup
    ConsumerRecordFactory<String, Payment> factory = new ConsumerRecordFactory<>(
            topic,
            new StringSerializer(),
            new Payment.Serde().serializer()
    );

    // processor is already waiting

    // test
    Payment payment = new Payment("txnId", "id","from","to", new BigDecimal(123.0), Payment.State.debit, 0);
    payment.setStateAndId(Payment.State.debit);


    new Thread(() -> {
      testDriver.pipeInput(factory.create(topic, "key", payment));
      testDriver.close();
    }).start();

    log("waiting...");

    Thread.sleep(100);
    // processor should be waiting
    assertTrue(waiting);
    assertEquals(0, counted);

    log("sleeping...");
    Thread.sleep(1000);


    log("waking...");
    synchronized (this) {
      this.notify();
    }
    Thread.sleep(100);


    // verify
    Assert.assertFalse(waiting);
    assertEquals(1, counted);

  }

  private void log(String msg) {
    System.out.println(new Date() + " :" + msg);
  }

  private boolean isProcessingRunning() {
    // if not running then block on a wait condition
    waiting = true;
    try {
      synchronized (this) {
        this.wait();
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    waiting = false;
    return true;
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
