package io.confluent.developer.livestreams.service;

import com.google.protobuf.ByteString;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bsideup.liiklus.GRPCLiiklusClient;
import com.github.bsideup.liiklus.protocol.LiiklusEvent;
import com.github.bsideup.liiklus.protocol.PublishRequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class ServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(ServiceApplication.class, args);
  }

}


@RestController
class Publisher {


  @PostMapping("/publish")
  public Mono<String> publish(@RequestBody final String param) throws JsonProcessingException {
    final NettyChannelBuilder builder = NettyChannelBuilder.forTarget("193.122.4.88:6565");
    final ManagedChannel channel = builder.usePlaintext().build();
    var topic = "events";

    final String message = new ObjectMapper().writeValueAsString(Map.of( 
        "deviceId", param)
    );
    LiiklusEvent event = LiiklusEvent
        .newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSource("livestreams")
        .setType("io.confluent.developer.livestreams.device.added")
        .setDataContentType("application/json")
        .setData(ByteString.copyFromUtf8(message))
        .build();
    final PublishRequest request = PublishRequest.newBuilder().setTopic(topic).setLiiklusEvent(event).build();
    return new GRPCLiiklusClient(channel).publish(request).map(Objects::toString);
  }
}