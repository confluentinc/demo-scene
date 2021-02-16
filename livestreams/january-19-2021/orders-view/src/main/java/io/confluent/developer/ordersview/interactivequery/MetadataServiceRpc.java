package io.confluent.developer.ordersview.interactivequery;


import com.google.protobuf.Empty;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

import java.util.Arrays;
import java.util.List;

import io.confluent.developer.livestreams.iq.proto.HostStoreInfo;
import io.confluent.developer.livestreams.iq.proto.HostStoreInfoList;
import io.confluent.developer.livestreams.iq.proto.IQServiceRequest;
import io.confluent.developer.livestreams.iq.proto.InteractiveQueryGrpc;
import io.confluent.developer.livestreams.iq.proto.OrderResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

import static org.apache.kafka.streams.StoreQueryParameters.fromNameAndType;
import static org.apache.kafka.streams.state.QueryableStoreTypes.keyValueStore;

@GRpcService
@RequiredArgsConstructor
public class MetadataServiceRpc extends InteractiveQueryGrpc.InteractiveQueryImplBase {

  private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;
  private final HostInfo hostInfo;
  private MetadataService metadataService;

  @EventListener(ContextRefreshedEvent.class)
  public void refresh() {
    this.metadataService = new MetadataService(streamsBuilderFactoryBean.getKafkaStreams());
  }

  @Override
  public void getMetadataForStore(final IQServiceRequest request,
                                  final StreamObserver<HostStoreInfoList> responseObserver) {
    
    final List<HostStoreInfo> hostStoreInfos = this.metadataService.streamsMetadataForStore(request.getStoreName());
    responseObserver.onNext(HostStoreInfoList.newBuilder().addAllMetadatas(hostStoreInfos).build());
    responseObserver.onCompleted();
  }

  @Override
  public void getAllMetadata(final Empty request, final StreamObserver<HostStoreInfoList> responseObserver) {
    final List<HostStoreInfo> hostStoreInfos = this.metadataService.streamsMetadata();
    responseObserver.onNext(HostStoreInfoList.newBuilder().addAllMetadatas(hostStoreInfos).build());
    responseObserver.onCompleted();
  }

  @Override
  public void getMetadataForStoreAndKey(final IQServiceRequest request,
                                        final StreamObserver<HostStoreInfoList> responseObserver) {
    final HostStoreInfo metadata =
        this.metadataService.streamsMetadataForStoreAndKey(
            request.getStoreName(),
            request.getKey(),
            Serdes.Long().serializer());

    responseObserver.onNext(HostStoreInfoList.newBuilder().addMetadatas(metadata).build());
    responseObserver.onCompleted();
  }

  @Override
  public void getOrder(final IQServiceRequest request, final StreamObserver<OrderResponse> responseObserver) {
    final KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
    final String storeName = request.getStoreName();
    final ReadOnlyKeyValueStore<Long, String> store = kafkaStreams.store(fromNameAndType(storeName, keyValueStore()));
    final long id = request.getKey();
    final String s = store.get(id);

    final OrderResponse order;

    final HostStoreInfo
        hostStoreInfo =
        metadataService.streamsMetadataForStoreAndKey(storeName, id, Serdes.Long().serializer());

    if (!thisHost(hostStoreInfo)) {
      // remote call 
      final ManagedChannel channel = ManagedChannelBuilder
          .forAddress(hostStoreInfo.getHost(), hostStoreInfo.getPort())
          .usePlaintext()
          .build();
      final InteractiveQueryGrpc.InteractiveQueryBlockingStub stub = InteractiveQueryGrpc.newBlockingStub(channel);
      order = stub.getOrder(request);
      channel.shutdown();
    } else {

      // local call
      order = OrderResponse.newBuilder()
          .addAllItems(Arrays.asList(s.split(", ")))
          .build();
    }

    responseObserver.onNext(order);
    responseObserver.onCompleted();
  }

  private boolean thisHost(final HostStoreInfo host) {
    return host.getHost().equals(hostInfo.host()) && host.getPort() == hostInfo.port();
  }
}
