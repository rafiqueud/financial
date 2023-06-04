package launcher.integration;


import application.dto.BalanceDTO;
import application.dto.CreateAccountDTO;
import application.dto.CreateMovementDTO;
import application.dto.TransferMovementDTO;
import domain.model.Account;
import domain.model.Movement;
import domain.model.MovementType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import launcher.FinancialApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import static domain.model.MovementDescription.CREDIT_TRANSFER_DESCRIPTION;
import static domain.model.MovementDescription.DEBIT_TRANSFER_DESCRIPTION;

@SpringBootTest(properties = {
        "spring.r2dbc.url=r2dbc:h2:mem:///./testdb",
        "spring.liquibase.url=jdbc:h2:mem:./testdb;DB_CLOSE_DELAY=-1"
})
@AutoConfigureWebTestClient
@ContextConfiguration(classes = {FinancialApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IntegrationTests {


    @Autowired
    private WebTestClient webClient;

    private static final AtomicReference<String> accountId1 = new AtomicReference<>();

    private static final AtomicReference<String> accountId2 = new AtomicReference<>();

    private static final BigDecimal expectedAmount = BigDecimal.TEN.setScale(2, RoundingMode.DOWN);

    private static final BigDecimal expectedZero = BigDecimal.ZERO.setScale(2, RoundingMode.DOWN);

    @Order(1)
    @Test
    public void createAccountsWithSuccess() {
        final var accountId1id = webClient.post()
                .uri("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(createAccountDTO()))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Account.class)
                .returnResult()
                .getResponseBody()
                .id();

        accountId1.set(accountId1id);

        final var accountId2Id = webClient.post()
                .uri("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(createAccountDTO()))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Account.class)
                .returnResult()
                .getResponseBody()
                .id();

        accountId2.set(accountId2Id);
    }

    @Order(2)
    @Test
    public void createDepositsWithSuccess() {
        webClient.post()
                .uri("/api/v1/movements/" + accountId1.get() + "/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(createMovementDTO()))
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Movement.class);

        webClient.post()
                .uri("/api/v1/movements/" + accountId2.get() + "/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(createMovementDTO()))
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Movement.class);
    }

    @Order(3)
    @Test
    public void createWithdrawsWithSuccess() {
        webClient.post()
                .uri("/api/v1/movements/" + accountId1.get() + "/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(createMovementDTO()))
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Movement.class);

        webClient.post()
                .uri("/api/v1/movements/" + accountId2.get() + "/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(createMovementDTO()))
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Movement.class);
    }

    @Order(4)
    @Test
    public void retrieveCreditMovements() {
        final var account1Movements = webClient.get()
                .uri("/api/v1/movements/" + accountId1.get() + "/credit")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Movement[].class)
                .returnResult()
                .getResponseBody();

        final var account2Movements = webClient.get()
                .uri("/api/v1/movements/" + accountId2.get() + "/credit")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Movement[].class)
                .returnResult()
                .getResponseBody();

        Assertions.assertEquals(1, account1Movements.length);
        Assertions.assertEquals(1, account2Movements.length);

        Assertions.assertEquals(expectedAmount, account1Movements[0].amount());
        Assertions.assertEquals(expectedAmount, account2Movements[0].amount());
    }

    @Order(5)
    @Test
    public void retrieveDebitMovements() {
        final var account1Movements = webClient.get()
                .uri("/api/v1/movements/" + accountId1.get() + "/debit")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Movement[].class)
                .returnResult()
                .getResponseBody();

        final var account2Movements = webClient.get()
                .uri("/api/v1/movements/" + accountId2.get() + "/debit")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Movement[].class)
                .returnResult()
                .getResponseBody();

        Assertions.assertEquals(1, account1Movements.length);
        Assertions.assertEquals(1, account2Movements.length);

        Assertions.assertEquals(expectedAmount, account1Movements[0].amount());
        Assertions.assertEquals(expectedAmount, account2Movements[0].amount());
    }

    @Order(6)
    @Test
    public void assertAccountsBalances() {
        final var account1Balance = webClient.get()
                .uri("/api/v1/accounts/" + accountId1.get() + "/balance")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(BalanceDTO.class)
                .returnResult()
                .getResponseBody();

        final var account2Balance = webClient.get()
                .uri("/api/v1/accounts/" + accountId2.get() + "/balance")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(BalanceDTO.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertEquals(expectedZero, account1Balance.balance());
        Assertions.assertEquals(expectedZero, account2Balance.balance());
    }

    @Order(7)
    @Test
    public void assertTransfersWorkWithSuccess() {
        // deposit 10 bucks in each account
        createDepositsWithSuccess();

        final var transferMovement = new TransferMovementDTO(
                accountId2.get(),
                expectedAmount
        );

        webClient.post()
                .uri("/api/v1/movements/" + accountId1.get() + "/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(transferMovement))
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Movement.class);

        final var account1Balance = webClient.get()
                .uri("/api/v1/accounts/" + accountId1.get() + "/balance")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(BalanceDTO.class)
                .returnResult()
                .getResponseBody();

        final var account2Balance = webClient.get()
                .uri("/api/v1/accounts/" + accountId2.get() + "/balance")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(BalanceDTO.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertEquals(expectedAmount.add(expectedAmount), account2Balance.balance());
        Assertions.assertEquals(expectedZero, account1Balance.balance());

        final var account2CreditMovements = webClient.get()
                .uri("/api/v1/movements/" + accountId2.get() + "/credit")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Movement[].class)
                .returnResult()
                .getResponseBody();

        final var account1DebitMovements = webClient.get()
                .uri("/api/v1/movements/" + accountId1.get() + "/debit")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Movement[].class)
                .returnResult()
                .getResponseBody();

        final var firstAccount2CreditMovement = account2CreditMovements[0];
        final var firstAccount1DebitMovement = account1DebitMovements[0];
        Assertions.assertEquals(firstAccount1DebitMovement.amount(), firstAccount2CreditMovement.amount());

        Assertions.assertEquals(firstAccount1DebitMovement.description(), String.format(DEBIT_TRANSFER_DESCRIPTION, accountId2.get()));
        Assertions.assertEquals(firstAccount2CreditMovement.description(), String.format(CREDIT_TRANSFER_DESCRIPTION, accountId1.get()));
    }

    @Order(8)
    @Test
    public void assertTransfersWorkWithSuccessWithConcurrentParallelProcessing() {

        IntStream.rangeClosed(0, 10)
                .parallel()
                .forEach(i -> {
                    try {
                        assertTransfersWorkWithSuccess();
                    } catch (final Throwable ignored) {
                    }
                });

        final var account1Movements = webClient.get()
                .uri("/api/v1/movements/" + accountId1.get() + "?start=2000-10-31&end=3000-10-31&page=0&pageSize=100")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Movement[].class)
                .returnResult()
                .getResponseBody();

        final var account1Balance = webClient.get()
                .uri("/api/v1/accounts/" + accountId1.get() + "/balance")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(BalanceDTO.class)
                .returnResult()
                .getResponseBody();


        // based on the order of tests and the number of the intStream range transfer processes. The balance of account 1 must be zero.
        final var account1BalanceOfMovements = retrieveTotalBalanceFromMovements(account1Movements);
        Assertions.assertEquals(account1Balance.balance(), account1BalanceOfMovements);
        Assertions.assertEquals(expectedZero, account1BalanceOfMovements);

        final var account2Movements = webClient.get()
                .uri("/api/v1/movements/" + accountId2.get() + "?start=2000-10-31&end=3000-10-31&page=0&pageSize=100")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Movement[].class)
                .returnResult()
                .getResponseBody();

        final var account2Balance = webClient.get()
                .uri("/api/v1/accounts/" + accountId2.get() + "/balance")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(BalanceDTO.class)
                .returnResult()
                .getResponseBody();

        final var account2BalanceOfMovements = retrieveTotalBalanceFromMovements(account2Movements);
        Assertions.assertEquals(account2Balance.balance(), account2BalanceOfMovements);

        // based on the order of tests and the number of the intStream range transfer processes. The balance of account 2 must be 240.
        final var expectedTotal = BigDecimal.valueOf(240).setScale(2, RoundingMode.FLOOR);
        Assertions.assertEquals(expectedTotal, account2BalanceOfMovements);

    }

    private BigDecimal retrieveTotalBalanceFromMovements(final Movement[] movements) {
        return Arrays.stream(movements)
                .map(movement -> {
                    if (movement.type().equals(MovementType.CREDIT)) {
                        return movement.amount();
                    } else {
                        return movement.amount().negate();
                    }
                }).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.DOWN);
    }

    private CreateMovementDTO createMovementDTO() {
        return new CreateMovementDTO(
                expectedAmount
        );
    }

    private CreateAccountDTO createAccountDTO() {
        return new CreateAccountDTO(
                UUID.randomUUID().toString(),
                BigDecimal.ZERO
        );
    }


}
