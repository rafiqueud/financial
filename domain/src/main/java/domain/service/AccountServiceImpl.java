package domain.service;

import domain.exception.AccountNotFoundException;
import domain.model.Account;
import domain.ports.persistence.AccountPersistencePort;
import domain.ports.service.AccountServicePort;
import java.math.BigDecimal;
import reactor.core.publisher.Mono;

public class AccountServiceImpl implements AccountServicePort {

    private final AccountPersistencePort accountPersistencePort;

    public AccountServiceImpl(final AccountPersistencePort accountPersistencePort) {
        this.accountPersistencePort = accountPersistencePort;
    }

    @Override
    public Mono<Account> createNewAccount(final String name, final BigDecimal limit) {
        return accountPersistencePort.createNewAccount(new Account(null, name, BigDecimal.ZERO, limit));
    }

    @Override
    public Mono<Account> findAccountById(final String id) {
        return accountPersistencePort.findAccountById(id)
                .switchIfEmpty(Mono.error(new AccountNotFoundException(id)));
    }

    @Override
    public Mono<BigDecimal> retrieveBalanceFromAccountId(final String id) {
        return accountPersistencePort.findBalanceByAccountId(id)
                .switchIfEmpty(Mono.error(new AccountNotFoundException(id)));
    }
}
