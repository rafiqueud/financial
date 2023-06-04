package domain.ports.service;

import domain.model.Account;
import java.math.BigDecimal;
import reactor.core.publisher.Mono;

public interface AccountServicePort {

    Mono<Account> createNewAccount(final String name, final BigDecimal limit);

    Mono<Account> findAccountById(final String id);

    Mono<BigDecimal> retrieveBalanceFromAccountId(final String id);
}
