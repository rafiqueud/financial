package application.controller;

import application.dto.CreateAccountDTO;
import application.dto.ErrorDTO;
import application.exceptions.ApiAdviceHandler;
import domain.exception.AccountNotFoundException;
import domain.model.Account;
import domain.ports.service.AccountServicePort;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import static application.exceptions.ErrorMessages.ACCOUNT_NOT_FOUND;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

@WebFluxTest(controllers = AccountController.class)
@ContextConfiguration(classes = {AccountController.class, ApiAdviceHandler.class})
class AccountControllerTests {

    @MockBean
    AccountServicePort accountServicePort;

    @Autowired
    private WebTestClient webClient;

    @Test
    public void testCreateNewAccount() {
        final var createAccountDto = new CreateAccountDTO(
                UUID.randomUUID().toString(),
                BigDecimal.ZERO
        );
        final var account = new Account(
                UUID.randomUUID().toString(),
                createAccountDto.name(),
                BigDecimal.ZERO,
                createAccountDto.limit()
        );

        Mockito.when(accountServicePort.createNewAccount(
                eq(createAccountDto.name()),
                eq(createAccountDto.limit()))
        ).thenReturn(Mono.just(account));

        webClient.post()
                .uri("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(createAccountDto))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Account.class)
                .isEqualTo(account);

        Mockito.verify(accountServicePort, times(1))
                .createNewAccount(createAccountDto.name(), createAccountDto.limit());
    }

    @Test
    public void testCreateNewAccountBadRequest() {

        final var createAccountDto = new CreateAccountDTO(
                null,
                null
        );

        webClient.post()
                .uri("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(createAccountDto))
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(ErrorDTO.class);

    }

    @Test
    public void testGetAccountNotFound() {
        final var accountId = UUID.randomUUID().toString();

        Mockito.when(accountServicePort.findAccountById(eq(accountId))).thenReturn(Mono.error(new AccountNotFoundException(accountId)));

        webClient.get()
                .uri("/api/v1/accounts/" + accountId)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(ErrorDTO.class)
                .isEqualTo(
                        new ErrorDTO(
                                404,
                                String.format(ACCOUNT_NOT_FOUND, accountId)
                        )
                );

    }

}