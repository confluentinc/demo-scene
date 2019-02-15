package io.confluent.kpay;

import io.confluent.common.utils.TestUtils;
import io.confluent.kpay.control.Controllable;
import io.confluent.kpay.control.PauseControllable;
import io.confluent.kpay.control.StartStopController;
import io.confluent.kpay.control.model.Status;
import io.confluent.kpay.metrics.PaymentsThroughput;
import io.confluent.kpay.payments.AccountProcessor;
import io.confluent.kpay.payments.PaymentsConfirmed;
import io.confluent.kpay.payments.PaymentsInFlight;
import io.confluent.kpay.payments.model.Payment;
import io.confluent.kpay.util.KafkaTopicClient;
import io.confluent.kpay.util.KafkaTopicClientImpl;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;

public class KPayAllInOneImpl implements KPay {
    private static final Logger log = LoggerFactory.getLogger(KPayAllInOneImpl.class);

    private String paymentsIncomingTopic = "kpay.payments.incoming";
    private String paymentsInflightTopic = "kpay.payments.inflight";;
    private String paymentsCompleteTopic = "kpay.payments.complete";;
    private String paymentsConfirmedTopic = "kpay.payments.confirmed";;
    private String bootstrapServers;

    private KafkaTopicClient topicClient;


    private long instanceId = System.currentTimeMillis();
    private PaymentsInFlight paymentsInFlight;

    // start stop status events to control the flow
    private String controlStatusTopic = "kpay.control.status";


    private PaymentsThroughput instrumentationThroughput;
    private AccountProcessor paymentAccountProcessor;
    private PaymentsConfirmed paymentsConfirmed;
    private AdminClient adminClient;
    private StartStopController controllerStartStop;
    private ScheduledFuture future;
    private PaymentRunnable paymentCommand;

    public KPayAllInOneImpl(String bootstrapServers) {

        this.bootstrapServers = bootstrapServers;
    }

    /**
     * Fire up all of the stream processors
     */
    public void start() {

        Controllable pauseController = new PauseControllable();

        startPaymentPipeline(pauseController);

        startController(pauseController);

        startInstrumentation();
    }

    private void startController(Controllable pauseController) {
        controllerStartStop = new StartStopController(controlStatusTopic, getControlProperties(bootstrapServers), pauseController);
        controllerStartStop.start();
    }

    private void startInstrumentation() {
        instrumentationThroughput = new PaymentsThroughput(paymentsCompleteTopic, getPaymentsProperties(bootstrapServers));
        instrumentationThroughput.start();
    }

    private void startPaymentPipeline(Controllable pauseController) {
        paymentsInFlight = new PaymentsInFlight(paymentsIncomingTopic, paymentsInflightTopic, paymentsCompleteTopic, getPaymentsProperties(bootstrapServers), pauseController);
        paymentsInFlight.start();

        paymentAccountProcessor = new AccountProcessor(paymentsInflightTopic, paymentsCompleteTopic, getPaymentsProperties(bootstrapServers));
        paymentAccountProcessor.start();

        paymentsConfirmed = new PaymentsConfirmed(paymentsCompleteTopic, paymentsConfirmedTopic, getPaymentsProperties(bootstrapServers));
        paymentsConfirmed.start();
    }

    @Override
    public String status() {
        return controllerStartStop.status();
    }
    @Override
    public String pause() {
        controllerStartStop.pause();
        return controllerStartStop.status();
    }
    @Override
    public String resume() {
        controllerStartStop.resume();
        return controllerStartStop.status();
    }


    @Override
    public void generatePayments() {

        KafkaProducer<String, Payment> producer =
                new KafkaProducer<>(properties(), new StringSerializer(), new Payment.Serde().serializer());

        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        paymentCommand = new PaymentRunnable() {

            int position;
            String[] from = new String[]{"larry", "joe", "mary", "bob"};
            String[] to = new String[]{"allan", "alex", "andrian", "ally"};

            @Override
            public void run() {

                Payment payment = new Payment("paymemt-" + System.currentTimeMillis(), System.currentTimeMillis() + "", from[position % from.length], to[position % from.length], Math.random() * 100, Payment.State.incoming);
                log.info("Send:" + payment);
                producer.send(buildRecord(paymentsIncomingTopic, System.currentTimeMillis(), payment.getId(), payment));
                position++;
                producer.flush();
            }
            public void stop() {
                log.info("Stopping payment generation");
                producer.close();
            }

        };
        future = scheduledExecutor.scheduleAtFixedRate(paymentCommand, 10, 2, TimeUnit.SECONDS);
        producer.close();
    }


    @Override
    public void stopPayments() {
        future.cancel(false);
        paymentCommand.stop();
    }

    private <V> ProducerRecord<String, Payment> buildRecord(String topicName,
                                                      Long timestamp,
                                                      String key, Payment payment) {
        return new ProducerRecord<>(topicName, null, timestamp,  key, payment);
    }

    @Override
    public String viewMetrics() {
        return instrumentationThroughput.getStats().toString();
    }

    private Properties getPaymentsProperties(String broker) {
        return getProperties(broker, Serdes.String().getClass().getName(), Payment.Serde.class.getName());
    }

    private Properties getControlProperties(String broker) {
       return getProperties(broker, Serdes.String().getClass().getName(), Status.Serde.class.getName());
    }
    private Properties getProperties(String broker, String keySerdesClass, String valueSerdesClass) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "TEST-APP-ID-" + instanceId++);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, keySerdesClass);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, valueSerdesClass);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 2000);
        return props;
    }

    public void initializeEnvironment() {
        getTopicClient().createTopic(paymentsInflightTopic, 5, (short)1);
        getTopicClient().createTopic(paymentsIncomingTopic, 5, (short) 1);
        getTopicClient().createTopic(paymentsCompleteTopic, 5, (short) 1);
        getTopicClient().createTopic(paymentsConfirmedTopic, 5, (short) 1);
        getTopicClient().createTopic(controlStatusTopic, 5, (short) 1);
    }

    private KafkaTopicClient getTopicClient() {
        if (topicClient == null) {
            Map<String, Object> configMap = new HashMap<>();
            configMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            configMap.put("application.id", "KPAY");
            configMap.put("commit.interval.ms", 0);
            configMap.put("cache.max.bytes.buffering", 0);
            configMap.put("auto.offset.reset", "earliest");
            configMap.put(StreamsConfig.STATE_DIR_CONFIG, TestUtils.tempDirectory().getPath());

            this.adminClient = AdminClient.create(configMap);
            this.topicClient = new KafkaTopicClientImpl(adminClient);
        }
        return this.topicClient;
    }

    private Properties properties() {
        Properties producerConfig = new Properties();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerConfig.put(ProducerConfig.ACKS_CONFIG, "all");
        producerConfig.put(ProducerConfig.RETRIES_CONFIG, 0);
        return producerConfig;
    }

    private interface PaymentRunnable extends Runnable {
        void stop();

    }

}
