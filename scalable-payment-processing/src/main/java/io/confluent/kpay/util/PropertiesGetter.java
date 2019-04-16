package io.confluent.kpay.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.streams.StreamsConfig;

public class PropertiesGetter {

    private static long instanceId = System.currentTimeMillis();
    private static String thisIpAddress = getIpAddress();


    public static Properties getProperties(String broker, String keySerdesClass, String valueSerdesClass) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "TEST-APP-ID-" + instanceId++);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, keySerdesClass);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, valueSerdesClass);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 2000);
        return props;
    }

    public static String getIpAddress() {
        if (thisIpAddress == null) {
            InetAddress thisIp = null;
            try {
                thisIp = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            thisIpAddress = thisIp.getHostAddress().toString();
        }
        return thisIpAddress;
    }

}
