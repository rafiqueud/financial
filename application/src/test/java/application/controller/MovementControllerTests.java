package application.controller;

import application.dto.CreateMovementDTO;
import application.dto.ErrorDTO;
import application.exceptions.ApiAdviceHandler;
import domain.exception.InsufficientBalanceException;
import domain.model.Movement;
import domain.model.MovementType;
import domain.ports.service.MovementServicePort;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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

import static application.exceptions.ErrorMessages.INSUFFICIENT_BALANCE;
import static domain.model.MovementDescription.WITHDRAW_DESCRIPTION;

@WebFluxTest(controllers = MovementController.class)
@ContextConfiguration(classes = {MovementController.class, ApiAdviceHandler.class})
public class MovementControllerTests {

    @MockBean
    MovementServicePort movementServicePort;

    @Autowired
    private WebTestClient webClient;

    @Test
    public void testWithdrawWithSuccess() {

        final var accountId = UUID.randomUUID().toString();
        final var createMovement = new CreateMovementDTO(
                BigDecimal.TEN
        );

        final var expectedMovement = new Movement(
                UUID.randomUUID().toString(),
                accountId,
                WITHDRAW_DESCRIPTION,
                createMovement.amount(),
                MovementType.DEBIT,
                LocalDateTime.now()
        );

        Mockito.when(movementServicePort.withdraw(accountId, createMovement.amount()))
                .thenReturn(Mono.just(expectedMovement));

        webClient.post()
                .uri("/api/v1/movements/" + accountId + "/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(createMovement))
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Movement.class)
                .isEqualTo(expectedMovement);

    }

    @Test
    public void testWithdrawWithInvalidFoundError() {

        final var accountId = UUID.randomUUID().toString();
        final var createMovement = new CreateMovementDTO(
                BigDecimal.TEN
        );

        Mockito.when(movementServicePort.withdraw(accountId, createMovement.amount()))
                .thenReturn(Mono.error(InsufficientBalanceException::new));


        final var expectedError = new ErrorDTO(
                400,
                INSUFFICIENT_BALANCE
        );

        webClient.post()
                .uri("/api/v1/movements/" + accountId + "/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(createMovement))
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(ErrorDTO.class)
                .isEqualTo(expectedError);
    }

}
