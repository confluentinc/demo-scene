package io.confluent.osquery;

import cc.mallet.topics.ParallelTopicModel;
import io.confluent.common.Configurable;
import io.confluent.ksql.function.udaf.UdafDescription;
import io.confluent.ksql.function.udaf.UdafFactory;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@UdafDescription(name = "LDA", description = "scores a document")
public class AnomalyDetectionUDF implements Configurable {

    private ExecutorService exec = Executors.newSingleThreadExecutor();
    private Map<String, ?> map;
    private Future<ParallelTopicModel> future;

    @UdafFactory(description = "score a document")
    public double score(final String... values) {
        String doc = StringUtils.join(values, " ");
        if(!map.containsKey("endpoint")) return -1;
        return call(doc, map.get("endpoint").toString());
    }

    @Override
    public void configure(Map<String, ?> map) {
        this.map = map;
    }

    private double call(String message, String endpoint) {
        try {

            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");

            OutputStream os = conn.getOutputStream();
            os.write(message.getBytes());
            os.flush();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            StringBuffer sb = new StringBuffer();
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }

            conn.disconnect();

            return Double.parseDouble(sb.toString());

        } catch (MalformedURLException e) {

            e.printStackTrace();
            return -1;

        } catch (IOException e) {

            e.printStackTrace();
            return -1;
        }
    }
}
