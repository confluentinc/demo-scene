package io.confluent.kpay.control;

import io.confluent.kpay.control.model.Status;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.Test;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.*;

import static org.junit.Assert.*;

public class StartStopControllerTest {

    @Test
    public void waitControlableShouldWaitAndNotify() throws InterruptedException, ExecutionException, TimeoutException {
        PauseControllable controllable = new PauseControllable();

        controllable.pauseProcessing();
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<?> task = executor.submit((Runnable) () -> controllable.pauseMaybe());

        Thread.sleep(200);

        assertFalse("Task hasnt exited", task.isDone());

        assertTrue("value was:" + controllable.getWaitingElaspedMs(), controllable.getWaitingElaspedMs() > 200);

        Thread.sleep(200);

        // release the paused task, resume processing
        controllable.startProcessing();

        assertEquals(0, controllable.getWaitingElaspedMs());


        task.get(1, TimeUnit.SECONDS);
        assertTrue("Task hasnt exited", task.isDone());
    }

    @Test
    public void shouldPauseResumePauseProcessing() throws InterruptedException {

        String topic = "meh";
        PauseControllable controllable = new PauseControllable();

        Properties properties = getProperties("localhost:9091");
        StartStopController controller = new StartStopController(topic, properties, controllable);
        controller.start();
        Topology topology = controller.getTopology();

        TopologyTestDriver testDriver = new TopologyTestDriver(topology, properties);

        // setup
        ConsumerRecordFactory<String, Status> factory = new ConsumerRecordFactory<>(
                topic,
                new StringSerializer(),
                new Status.Serde().serializer()
        );

        /**
         * Pause it
         */
        sendMessage(topic, testDriver, factory, Status.Code.PAUSE);

        log("waiting...");

        Thread.sleep(100);

        assertTrue(controllable.isPaused());

        /**
         * Run it
         */
        sendMessage(topic, testDriver, factory, Status.Code.START);


        Thread.sleep(100);
        assertFalse(controllable.isPaused());


        /**
         * Pause it
         */
        sendMessage(topic, testDriver, factory, Status.Code.PAUSE);

        Thread.sleep(100);
        assertTrue(controllable.isPaused());


        testDriver.close();

    }

    private void sendMessage(String topic, TopologyTestDriver testDriver, ConsumerRecordFactory<String, Status> factory, Status.Code pause) {
        new Thread(() -> {
            testDriver.pipeInput(factory.create(topic, "key", new Status(pause)));

        }).start();
    }


    private void log(String msg) {
        System.out.println(new Date() + " :" + msg);
    }

    private Properties getProperties(String broker) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "TEST-APP-ID");// + System.currentTimeMillis());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Status.Serde.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 2000);
        return props;
    }

}