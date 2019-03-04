package io.confluent.kpay;

import io.confluent.common.utils.TestUtils;
import io.confluent.kpay.control.Controllable;
import io.confluent.kpay.control.PauseControllable;
import io.confluent.kpay.control.StartStopController;
import io.confluent.kpay.control.model.Status;
import io.confluent.kpay.ktablequery.KTableResourceEndpoint;
import io.confluent.kpay.ktablequery.WindowKTableResourceEndpoint;
import io.confluent.kpay.metrics.PaymentsThroughput;
import io.confluent.kpay.metrics.model.ThroughputStats;
import io.confluent.kpay.payments.AccountProcessor;
import io.confluent.kpay.payments.PaymentsConfirmed;
import io.confluent.kpay.payments.PaymentsInFlight;
import io.confluent.kpay.payments.model.AccountBalance;
import io.confluent.kpay.payments.model.ConfirmedStats;
import io.confluent.kpay.payments.model.InflightStats;
import io.confluent.kpay.payments.model.Payment;
import io.confluent.kpay.util.KafkaTopicClient;
import io.confluent.kpay.util.KafkaTopicClientImpl;
import io.confluent.kpay.util.Pair;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.state.StreamsMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static java.util.Collections.*;

public class KPayAllInOneImpl implements KPay {
    private static final Logger log = LoggerFactory.getLogger(KPayAllInOneImpl.class);

    private String paymentsIncomingTopic = "kpay.payments.incoming";
    private String paymentsInflightTopic = "kpay.payments.inflight";
    private String paymentsCompleteTopic = "kpay.payments.complete";
    private String paymentsConfirmedTopic = "kpay.payments.confirmed";
    private String controlStatusTopic = "kpay.control.status";

    private String bootstrapServers;

    private KafkaTopicClient topicClient;

    private long instanceId = System.currentTimeMillis();


    private PaymentsInFlight paymentsInFlight;
    private AccountProcessor paymentAccountProcessor;
    private PaymentsConfirmed paymentsConfirmed;
    private PaymentsThroughput instrumentationThroughput;


    private AdminClient adminClient;
    private StartStopController controllerStartStop;
    private ScheduledFuture scheduledPaymentFuture;
    private PaymentRunnable paymentRunnable;
    private String thisIpAddress;

    public KPayAllInOneImpl(String bootstrapServers) {

        this.bootstrapServers = bootstrapServers;
    }

    /**
     * Fire up all of the stream processors
     */
    public void start() throws UnknownHostException {

        Controllable pauseController = new PauseControllable();

        startPaymentPipeline(pauseController);

        startController(pauseController);

        startInstrumentation();

        log.info("MetaStores:" + getMetaStores());
    }

    public String getMetaStores() {
        Collection<StreamsMetadata> streamsMetadata = paymentAccountProcessor.allMetaData();
        return streamsMetadata.toString();
    }

    private void startController(Controllable pauseController) {
        controllerStartStop = new StartStopController(controlStatusTopic, getControlProperties(bootstrapServers), pauseController);
        controllerStartStop.start();
    }

    private int instrumentationPortOffset = 21000;
    private void startInstrumentation() throws UnknownHostException {
        instrumentationThroughput = new PaymentsThroughput(paymentsCompleteTopic, getPaymentsProperties(bootstrapServers, instrumentationPortOffset++));
        instrumentationThroughput.start();
    }

    private int paymentsPortOffset = 20000;
    private void startPaymentPipeline(Controllable pauseController) throws UnknownHostException {
        paymentsInFlight = new PaymentsInFlight(paymentsIncomingTopic, paymentsInflightTopic, paymentsCompleteTopic, getPaymentsProperties(bootstrapServers, paymentsPortOffset++), pauseController);
        paymentsInFlight.start();

        paymentAccountProcessor = new AccountProcessor(paymentsInflightTopic, paymentsCompleteTopic, getPaymentsProperties(bootstrapServers, paymentsPortOffset++));
        paymentAccountProcessor.start();

        paymentsConfirmed = new PaymentsConfirmed(paymentsCompleteTopic, paymentsConfirmedTopic, getPaymentsProperties(bootstrapServers, paymentsPortOffset++));
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
    public String shutdown() {
        controllerStartStop.pause();
        paymentsInFlight.stop();
        paymentsConfirmed.stop();
        paymentAccountProcessor.stop();
        instrumentationThroughput.stop();

        return "shutdown processors complete";
    }


    @Override
    public void generatePayments(final int ratePerSecond) {

        KafkaProducer<String, Payment> producer =
                new KafkaProducer<>(properties(), new StringSerializer(), new Payment.Serde().serializer());

        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        paymentRunnable = new PaymentRunnable() {

            int position;
            String[] from = new String[]{"larry", "joe", "mary", "bob"};
            String[] to = new String[]{"allan", "alex", "adrian", "ally"};

            @Override
            public void run() {

                try {
                    Payment payment = new Payment("pay-" + System.currentTimeMillis(), System.currentTimeMillis() + "", from[position % from.length], to[position % from.length], new BigDecimal(Math.round((Math.random() * 100.0)*100.0)/100.0), Payment.State.incoming, System.currentTimeMillis() );
                    log.info("Send:" + payment);
                    producer.send(buildRecord(paymentsIncomingTopic, System.currentTimeMillis(), payment.getId(), payment));
                    position++;
                    producer.flush();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            public void stop() {
                log.info("Stopping payment generation");
                producer.close();
            }
        };

        log.info("Generating Payments at:" + ratePerSecond);
        scheduledPaymentFuture = scheduledExecutor.scheduleAtFixedRate(paymentRunnable, 1000, ratePerSecond > 1000 ? 1 : 1000/ratePerSecond, TimeUnit.MILLISECONDS);
    }


    @Override
    public void stopPayments() {
        log.info("Stopping Payment generation");

        scheduledPaymentFuture.cancel(false);
        paymentRunnable.stop();
        scheduledPaymentFuture = null;
    }

    @Override
    public boolean isGeneratingPayments() {
        return scheduledPaymentFuture != null;
    }

    private <V> ProducerRecord<String, Payment> buildRecord(String topicName,
                                                            Long timestamp,
                                                            String key, Payment payment) {
        return new ProducerRecord<>(topicName, null, timestamp,  key, payment);
    }

    @Override
    public ThroughputStats viewMetrics() {
        return instrumentationThroughput.getStats();
    }


    @Override
    public List<AccountBalance> listAccounts() {
        KTableResourceEndpoint<String, AccountBalance> microRestService = paymentAccountProcessor.getMicroRestService();
        List<String> accountKeys = new ArrayList<>(microRestService.keySet());
        sort(accountKeys);
        List<Pair<String, AccountBalance>> pairs = microRestService.get(accountKeys);
        return pairs.stream().map(p -> p.getV()).collect(Collectors.toList());
    }

    public Pair<InflightStats, ConfirmedStats> getPaymentPipelineStats() {
        WindowKTableResourceEndpoint<String, InflightStats> paymentsInFlightSvc = paymentsInFlight.getMicroRestService();

        List<Pair<String, InflightStats>> inflightStats = paymentsInFlightSvc.get(new ArrayList<>(paymentsInFlightSvc.keySet()));

        WindowKTableResourceEndpoint<String, ConfirmedStats> paymentsConfirmedSvc = paymentsConfirmed.getMicroRestService();

        List<Pair<String, ConfirmedStats>> confirmedStats = paymentsConfirmedSvc.get(new ArrayList<>(paymentsConfirmedSvc.keySet()));

        if (inflightStats.size() == 0 || confirmedStats.size() == 0) return new Pair<>(new InflightStats(), new ConfirmedStats());

        return new Pair<>(inflightStats.get(0).getV(), confirmedStats.get(0).getV());
    }

    @Override
    public String showAccountDetails(String accountName) {
        return null;
    }

    private Properties getPaymentsProperties(String broker, int portOffset) throws UnknownHostException {
        Properties properties = getProperties(broker, Serdes.String().getClass().getName(), Payment.Serde.class.getName());
        // payment processors start from 20000
        properties.put(StreamsConfig.APPLICATION_SERVER_CONFIG, getIpAddress() + ":" + portOffset);
        System.out.println(" APP PORT:" + properties.get(StreamsConfig.APPLICATION_SERVER_CONFIG));
        return properties;
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

    private String getIpAddress() throws UnknownHostException {
        if (thisIpAddress == null) {
            InetAddress thisIp = InetAddress.getLocalHost();
            thisIpAddress = thisIp.getHostAddress().toString();
        }
        return thisIpAddress;
    }


    private interface PaymentRunnable extends Runnable {
        void stop();

    }

}
