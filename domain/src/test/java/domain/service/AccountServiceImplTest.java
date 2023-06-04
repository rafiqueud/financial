package domain.service;


import domain.exception.AccountNotFoundException;
import domain.model.Account;
import domain.ports.persistence.AccountPersistencePort;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @InjectMocks
    private AccountServiceImpl accountService;

    @Mock
    private AccountPersistencePort accountPersistencePort;


    @Test
    void findAccountByIdWithSuccess() {
        final var accountId = UUID.randomUUID().toString();
        final var account = new Account(
                accountId,
                UUID.randomUUID().toString(),
                BigDecimal.TEN,
                BigDecimal.ZERO
        );
        Mockito.when(accountPersistencePort.findAccountById(eq(accountId))).thenReturn(
                Mono.just(account)
        );
        StepVerifier.create(accountService.findAccountById(accountId))
                .assertNext(got -> Assertions.assertEquals(account, got))
                .verifyComplete();

    }

    @Test
    void findAccountByIdWithEmpty() {
        final var accountId = UUID.randomUUID().toString();

        Mockito.when(accountPersistencePort.findAccountById(eq(accountId))).thenReturn(
                Mono.empty()
        );
        StepVerifier.create(accountService.findAccountById(accountId))
                .expectError(AccountNotFoundException.class)
                .verify();
    }

    @Test
    void retrieveBalanceFromAccountIdWithSuccess() {
        final var accountId = UUID.randomUUID().toString();
        final var expected = BigDecimal.TEN;
        Mockito.when(accountPersistencePort.findBalanceByAccountId(eq(accountId))).thenReturn(
                Mono.just(expected)
        );
        StepVerifier.create(accountService.retrieveBalanceFromAccountId(accountId))
                .assertNext(got -> Assertions.assertEquals(got, expected))
                .verifyComplete();
    }

    @Test
    void retrieveBalanceFromAccountIdWithEmpty() {

        final var accountId = UUID.randomUUID().toString();

        Mockito.when(accountPersistencePort.findBalanceByAccountId(eq(accountId))).thenReturn(
                Mono.empty()
        );
        StepVerifier.create(accountService.retrieveBalanceFromAccountId(accountId))
                .expectError(AccountNotFoundException.class)
                .verify();
    }

}