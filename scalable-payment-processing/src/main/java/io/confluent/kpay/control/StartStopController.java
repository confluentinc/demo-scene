package io.confluent.kpay.control;

import io.confluent.kpay.control.model.Status;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;


/**
 * Starts and stops a Controllable instance
 */
public class StartStopController {

    private static final Logger log = LoggerFactory.getLogger(StartStopController.class);

    private final Topology topology;
    private Properties streamsConfig;
    private Controllable controllable;
    private KafkaStreams streams;
    boolean isProcessing = true;

    public StartStopController(String controllerTopic, Properties streamsConfig, Controllable controllable){
        this.streamsConfig = streamsConfig;
        this.controllable = controllable;


        /**
         * TODO: store history in a hopping window
         */
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Status> stream = builder.stream(controllerTopic);
        stream.foreach((key, value) -> {
            if (value.getCode() == Status.Code.PAUSE) {
                pause();

            } else if (value.getCode() == Status.Code.START) {
                resume();
            }
        });

        topology = builder.build();
    }

    public void resume() {
        log.info("Resume:" + controllable);
        controllable.startProcessing();
        isProcessing = true;
    }

    public void pause() {
        log.info("Pause:" + controllable);
        controllable.pauseProcessing();
        isProcessing = false;
    }


    public boolean isProcessing() {
        return isProcessing;
    }
    public void start() {
        streams = new KafkaStreams(topology, streamsConfig);
        streams.start();

        log.info(topology.describe().toString());
    }

    public void stop() {
        streams.close();
        streams.cleanUp();
    }

    public Topology getTopology() {
        return topology;
    }

    public String status() {
        return Boolean.toString(isProcessing);
    }
}
