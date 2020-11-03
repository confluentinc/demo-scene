package io.confluent.developer.ccloud.demo.kstream.data.domain.account;

import com.github.javafaker.Faker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static java.time.Duration.ofSeconds;
import static reactor.util.retry.Retry.backoff;

@Service
@Slf4j(topic = "Account Service")
public class AccountServiceImpl implements AccountService {

  private final AccountRepository accountRepository;
  private final int startingAccountNumber;
  private final int lastAccountNumber;

  public AccountServiceImpl(AccountRepository accountRepository,
                            @Value("${datafaker.account.starting-account-number:0}") int startingAccountNumber,
                            @Value("${datafaker.account.numbers:1000}") int numbersOfAccount) {
    this.accountRepository = accountRepository;

    this.startingAccountNumber = startingAccountNumber;
    this.lastAccountNumber = startingAccountNumber + numbersOfAccount;
  }

  @Override
  public void generateAccounts() {
    Faker faker = new Faker();

    Mono.<Page<Account>>create(
        sink -> sink.success(accountRepository.findByNumberBetweenOrderByNumberDesc(startingAccountNumber,
                                                                                    lastAccountNumber + 1,
                                                                                    PageRequest.of(0, 1))))
        .map(page -> page.getContent().size() == 0 ? startingAccountNumber
                                                   : page.getContent().get(0).getNumber())
        .filter(latestAccount -> latestAccount < lastAccountNumber)
        .flatMapMany(latestAccount -> Flux.range(latestAccount + 1, lastAccountNumber))
        .map(index -> new Account(index, faker.name().firstName(), faker.name().lastName(),
                                  faker.address().streetName(), faker.address().buildingNumber(),
                                  faker.address().city(),
                                  faker.address().country(), LocalDateTime.now(), LocalDateTime.now()))

        .buffer(10000)
        .doOnNext(accountRepository::saveAll)
        .doOnNext(accounts -> log.info("Generated account number {}.", accounts.get(accounts.size() - 1).getNumber()))
        .retryWhen(backoff(10, ofSeconds(1)))
        .doOnError(ex -> log.error("Failed to retry", ex))
        .subscribeOn(Schedulers.newSingle("Account-Generator"))
        .subscribe();
  }
}
