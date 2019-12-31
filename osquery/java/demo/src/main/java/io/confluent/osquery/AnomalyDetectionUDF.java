package io.confluent.osquery;

import io.confluent.common.Configurable;
import io.confluent.ksql.function.udf.Udf;
import io.confluent.ksql.function.udf.UdfDescription;
import io.confluent.ksql.function.udf.UdfParameter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@UdfDescription(name = "lda", description = "scores a document")
public class AnomalyDetectionUDF implements Configurable {

    private static final String ENDPOINT = "ksql.functions.lda.endpoint";
    private ExecutorService exec = Executors.newSingleThreadExecutor();
    private Map<String, String> map = new HashMap<>();
    private static Logger logger = LoggerFactory.getLogger(AnomalyDetectionUDF.class);

    @Udf(description = "score a document")
    public double lda(@UdfParameter(value = "values", description = "variadic string columns") final String... values) {
        if(values == null) return -2;
        try {
            String doc = StringUtils.join(values, " ");
            String endpoint = map.containsKey(ENDPOINT) ? map.get(ENDPOINT) : "http://model:4567/model/latest/score";
            return call(doc, endpoint);
        } catch (Exception e) {
            e.printStackTrace();
            return -1.1;
        }
    }

    @Override
    public void configure(Map<String, ?> map) {
        map.entrySet().stream().forEach((e) -> this.map.put(e.getKey(), e.getValue().toString()));
    }

    private double call(String message, String endpoint) throws IOException {

        logger.info(message);

        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "text/plain");
        conn.setConnectTimeout(50000);

        OutputStream os = conn.getOutputStream();
        os.write(message.getBytes());
        os.flush();

        BufferedReader br = new BufferedReader(new InputStreamReader(
                (conn.getInputStream())));

        String output;
        StringBuffer sb = new StringBuffer();
        while ((output = br.readLine()) != null) {
            sb.append(output);
        }

        conn.disconnect();

        return Double.parseDouble(sb.toString());
    }

    public static void main(String... args) throws IOException {
        AnomalyDetectionUDF udf = new AnomalyDetectionUDF();
        Map<String, String> conf = new HashMap<>();
        conf.put(ENDPOINT, "http://localhost:4567/model/latest/score");
        udf.configure(conf);

        Scanner sc = new Scanner(System.in);
        System.out.println("messages:");
        while(sc.hasNextLine()) {
            String message = sc.nextLine();
            double score = udf.call(message, conf.get(ENDPOINT));
            System.out.println(message+": "+score);
        }
    }
}
