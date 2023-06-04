package domain.ports.persistence;

import java.math.BigDecimal;
import domain.model.Account;
import reactor.core.publisher.Mono;

public interface AccountPersistencePort {

    Mono<Account> createNewAccount(final Account account);

    Mono<Account> findAccountById(final String id);

    Mono<BigDecimal> findBalanceByAccountId(final String accountId);

}
