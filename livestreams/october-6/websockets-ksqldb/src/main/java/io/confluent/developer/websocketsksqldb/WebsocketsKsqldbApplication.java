package io.confluent.developer.websocketsksqldb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.net.URL;
import java.util.Map;

import io.confluent.developer.ksqldb.reactor.ReactorClient;
import io.confluent.ksql.api.client.ClientOptions;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class WebsocketsKsqldbApplication {

  @Bean
  public HandlerMapping handlerMapping() {
    return new SimpleUrlHandlerMapping(Map.of("/websocket", new MyWebSocketHandler(ksqlReactorClient())), -1);
  }

  @Bean
  public WebSocketHandlerAdapter handlerAdapter() {
    return new WebSocketHandlerAdapter();
  }

  @Value("${KSQLDB_ENDPOINT}")
  String ksqlDbEndpointString;

  @Value("${KSQLDB_BASIC_AUTH_USER_INFO}")
  String basicAuthUserInfo;

  @SneakyThrows
  @Bean
  ReactorClient ksqlReactorClient() {

    final URL ksqldbEndpoint = new URL(this.ksqlDbEndpointString);

    return ReactorClient.fromOptions(ClientOptions.create()
                                         .setHost(ksqldbEndpoint.getHost())
                                         .setPort(ksqldbEndpoint.getPort())
                                         .setUseTls(true)
                                         .setUseAlpn(true) // should it be enabled by default?
                                         .setBasicAuthCredentials(basicAuthUserInfo.split(":")[0],
                                                                  basicAuthUserInfo.split(":")[1]));
  }


  public static void main(String[] args) {
    SpringApplication.run(WebsocketsKsqldbApplication.class, args);
  }

}


@RequiredArgsConstructor
class MyWebSocketHandler implements WebSocketHandler {

  final ReactorClient ksqlDbReactorClient;

  @Override
  public Mono<Void> handle(WebSocketSession session) {

    // simple Flux with generated data
    /*final Flux<String> flux = Flux.generate(synchronousSink -> {
      final String s = UUID.randomUUID().toString();
      synchronousSink.next(s);
    });

    final Flux<String> zip = Flux.interval(Duration.ofMillis(1000L)).zipWith(flux, (time, uuid) -> uuid);*/

    return session
        .send(ksqlDbReactorClient.streamQuery("select * from ACCOMPLISHED_FEMALE_READERS EMIT CHANGES;")
                  .map(row -> session.textMessage(row.toString())))
        .and(session.receive()
                 .map(WebSocketMessage::getPayloadAsText)
                 .log());
  }
}