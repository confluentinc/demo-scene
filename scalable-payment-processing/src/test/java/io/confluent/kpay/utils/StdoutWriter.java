package io.confluent.kpay.utils;

import io.prometheus.jmx.JmxScraper;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class StdoutWriter implements JmxScraper.MBeanReceiver {


    private String template = "{\n" +
            "  \"bizTag\": \"%s\",\n" +
            "  \"envTag\": \"%s\",\n" +
            "  \"host\": \"%s\",\n" +
            "  \"appId\": \"%s\",\n" +
            "  \"metric\": {\n" +
            "    \"resource\": \"%s\",\n" +
            "    \"name\": \"%s\",\n" +
            "    \"value\": %f,\n" +
            "    \"time\": %d\n" +
            "  }\n" +
            "}";
    private final String bizTag;
    private final String envTag;
    private String hostName;

    public StdoutWriter(String bizTag, String envTag) {
        this.bizTag = bizTag;
        this.envTag = envTag;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostName = "unknown";
        }
    }

    public void recordBean(
            String domain,
            LinkedHashMap<String, String> beanProperties,
            LinkedList<String> attrKeys,
            String attrName,
            String attrType,
            String attrDescription,
            Object value) {



        // REST: Path: [/2-tags/hostname ] Metric[ String app-context, String metric-name double value]
        //


        String format = String.format(template, bizTag, envTag, hostName, domain, attrKeys.toString(), getLabel(beanProperties), getNumeric(value), System.currentTimeMillis());


//        System.out.println(String.format("%s/%s [ %s %s %s %s: %s ]", bizTag + "/" + envTag, hostName, domain, beanProperties, attrKeys, attrName, value));
        System.out.println(format);


    }

    private String getLabel(LinkedHashMap<String, String> beanProperties) {
        return beanProperties.entrySet().stream().map(entry -> entry.getKey() + "-" + entry.getValue()).collect(Collectors.joining("."));
    }

    private double getNumeric(Object value) {
        double v = 0;
        if (value instanceof Long) {
            v = ((Long) value).doubleValue();
        } else if (value instanceof Double) {
            v = (double) value;
        } else if (value instanceof Integer) {
            v = ((Integer) value).doubleValue();
        } else {
            v = 0;
        }
        return v;
    }
}
