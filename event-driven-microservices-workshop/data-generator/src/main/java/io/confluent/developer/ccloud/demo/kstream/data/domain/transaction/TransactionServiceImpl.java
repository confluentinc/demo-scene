package io.confluent.developer.ccloud.demo.kstream.data.domain.transaction;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

import io.confluent.developer.ccloud.demo.kstream.topic.TransactionRequestTopicConfig;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

  private final KafkaTemplate<String, Transaction> transactionTemplate;
  private final TransactionRequestTopicConfig transactionRequestTopicConfig;
  
  @Override
  public Mono<Void> publishTransaction(TransactionRequest transactionRequest) {
    
    return Mono
        .fromFuture(transactionTemplate
                        .send(transactionRequestTopicConfig.getName(), transactionRequest.getAccount(),
                              new Transaction(UUID.randomUUID().toString(), transactionRequest.getAccount(),
                                              transactionRequest.getAmount(), transactionRequest.getType(),
                                              transactionRequest.getCurrency(), transactionRequest.getCountry()))
                        .completable())
        .then();
  }

}
