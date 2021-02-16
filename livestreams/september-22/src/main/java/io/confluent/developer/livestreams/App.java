package io.confluent.developer.livestreams;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.confluent.ksql.api.client.Client;
import io.confluent.ksql.api.client.ClientOptions;
import io.confluent.ksql.api.client.ExecuteStatementResult;
import io.confluent.ksql.api.client.TopicInfo;
import reactor.core.publisher.Mono;

class App {

  public static final String
      CREATE_TOPIC_STATEMENT =
      "CREATE STREAM cheese_shipments (shipmentId INT KEY, cheese VARCHAR, shipmentTimestamp VARCHAR) WITH (kafka_topic='cheese_shipments', partitions=3, value_format='json');";

  private final Client ksqlClient;

  public App(final Client ksqlClient) {
    this.ksqlClient = ksqlClient;
  }

  public static void main(String[] args) throws MalformedURLException {

    final URL ksqldbEndpoint = new URL(System.getenv("KSQLDB_ENDPOINT"));
    final String basicAuthUserInfo = System.getenv("KSQLDB_BASIC_AUTH_USER_INFO");

    final ClientOptions clientOptions = ClientOptions.create()
        .setHost(ksqldbEndpoint.getHost())
        .setPort(ksqldbEndpoint.getPort())
        .setUseTls(true)
        .setUseAlpn(true) // should it be enabled by default?
        .setBasicAuthCredentials(basicAuthUserInfo.split(":")[0],
                                 basicAuthUserInfo.split(":")[1]);

    final Client client = Client.create(clientOptions);
    final App app = new App(client);

    final Mono<List<TopicInfo>> listMono = app.listOfTopic();

    final Mono<ExecuteStatementResult> executeStatement = app.executeStatement(CREATE_TOPIC_STATEMENT);

    listMono
        .map(topicInfos -> {
          System.out.println("topicInfos = " + topicInfos);
          return topicInfos;
        })
        .then(executeStatement)
        .then(listMono)
        .subscribe(System.out::println);
  }

  private Mono<ExecuteStatementResult> executeStatement(final String sql) {
    return this.executeStatement(sql, Collections.emptyMap());
  }

  public Mono<List<TopicInfo>> listOfTopic() {
    return Mono.fromFuture(this.ksqlClient::listTopics);
  }

  public Mono<ExecuteStatementResult> executeStatement(String sql, Map<String, Object> properties) {
    final CompletableFuture<ExecuteStatementResult>
        completableFuture =
        this.ksqlClient.executeStatement(sql, properties);
    return Mono.fromFuture(() -> completableFuture);
  }
}

