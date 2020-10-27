package io.confluent.developer.ccloud.demo.kstream.data.domain.transaction;

import reactor.core.publisher.Mono;

public interface TransactionService {

  Mono<Void> publishTransaction(TransactionRequest transactionRequest);
}
