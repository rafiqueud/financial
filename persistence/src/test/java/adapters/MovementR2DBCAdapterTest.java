package adapters;

import domain.exception.AccountNotFoundException;
import domain.exception.InsufficientBalanceException;
import domain.model.MovementType;
import persistence.adapters.MovementR2DBCAdapter;
import persistence.entity.AccountEntity;
import persistence.entity.MovementEntity;
import persistence.mappers.MovementMapper;
import persistence.repository.AccountRepository;
import persistence.repository.MovementRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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

import static domain.model.MovementDescription.DEPOSIT_DESCRIPTION;
import static domain.model.MovementDescription.WITHDRAW_DESCRIPTION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;


@ExtendWith(MockitoExtension.class)
class MovementR2DBCAdapterTest {

    @InjectMocks
    MovementR2DBCAdapter movementR2DBCAdapter;

    @Mock
    AccountRepository accountRepository;

    @Mock
    MovementRepository movementRepository;

    @Mock
    MovementMapper movementMapper;

    @Test
    void depositWithSuccess() {
        // Arrange
        final var accountId = UUID.randomUUID();
        final var depositDate = LocalDateTime.now();
        final var amount = BigDecimal.TEN;

        final var accountEntity = new AccountEntity(
                accountId,
                UUID.randomUUID().toString(),
                BigDecimal.ONE,
                BigDecimal.ZERO
        );
        Mockito.when(accountRepository.findById(eq(accountId.toString())))
                .thenReturn(Mono.just(accountEntity));

        final var expectedSaveAccount = new AccountEntity(
                accountId,
                accountEntity.getName(),
                accountEntity.getBalance().add(amount),
                accountEntity.getLimit()
        );
        Mockito.when(accountRepository.save(eq(expectedSaveAccount)))
                .thenReturn(Mono.just(expectedSaveAccount));

        final var expectedMovement = new MovementEntity(
                null,
                accountId,
                DEPOSIT_DESCRIPTION,
                amount,
                MovementType.CREDIT,
                depositDate
        );
        final var savedMovementEntity = new MovementEntity(
                UUID.randomUUID(),
                expectedMovement.getAccountId(),
                expectedMovement.getDescription(),
                expectedMovement.getAmount(),
                expectedMovement.getType(),
                expectedMovement.getDate()
        );
        Mockito.when(movementRepository.save(eq(expectedMovement)))
                .thenReturn(Mono.just(savedMovementEntity));

        Mockito.when(movementMapper.toMovement(any())).thenCallRealMethod();

        // act and assert
        StepVerifier.create(movementR2DBCAdapter.deposit(accountId.toString(), amount, depositDate))
                .assertNext(movement -> {
                    Assertions.assertEquals(savedMovementEntity.getId().toString(), movement.id());
                    Assertions.assertEquals(savedMovementEntity.getAccountId().toString(), movement.account_id());
                    Assertions.assertEquals(savedMovementEntity.getType(), movement.type());
                    Assertions.assertEquals(savedMovementEntity.getAmount(), movement.amount());
                    Assertions.assertEquals(savedMovementEntity.getDescription(), movement.description());
                })
                .verifyComplete();
    }

    @Test
    void depositWithAccountNotFound() {
        // Arrange
        final var accountId = UUID.randomUUID().toString();
        final var depositDate = LocalDateTime.now();
        final var amount = BigDecimal.TEN;

        Mockito.when(accountRepository.findById(eq(accountId)))
                .thenReturn(Mono.empty());

        // act and assert
        StepVerifier.create(movementR2DBCAdapter.deposit(accountId, amount, depositDate))
                .expectError(AccountNotFoundException.class)
                .verify();
    }

    @Test
    void withdrawWithBalanceSuccess() {
        // Arrange
        final var accountId = UUID.randomUUID();
        final var depositDate = LocalDateTime.now();
        final var amount = BigDecimal.TEN;

        final var accountEntity = new AccountEntity(
                accountId,
                UUID.randomUUID().toString(),
                BigDecimal.TEN,
                BigDecimal.ZERO
        );
        Mockito.when(accountRepository.findById(eq(accountId.toString())))
                .thenReturn(Mono.just(accountEntity));

        final var expectedSaveAccount = new AccountEntity(
                accountId,
                accountEntity.getName(),
                BigDecimal.ZERO,
                accountEntity.getLimit()
        );
        Mockito.when(accountRepository.save(eq(expectedSaveAccount)))
                .thenReturn(Mono.just(expectedSaveAccount));

        final var expectedMovement = new MovementEntity(
                null,
                accountId,
                WITHDRAW_DESCRIPTION,
                amount,
                MovementType.DEBIT,
                depositDate
        );
        final var savedMovementEntity = new MovementEntity(
                UUID.randomUUID(),
                expectedMovement.getAccountId(),
                expectedMovement.getDescription(),
                expectedMovement.getAmount(),
                expectedMovement.getType(),
                expectedMovement.getDate()
        );
        Mockito.when(movementRepository.save(eq(expectedMovement)))
                .thenReturn(Mono.just(savedMovementEntity));

        Mockito.when(movementMapper.toMovement(any())).thenCallRealMethod();

        // act and assert
        StepVerifier.create(movementR2DBCAdapter.withdraw(accountId.toString(), amount, depositDate))
                .assertNext(movement -> {
                    Assertions.assertEquals(savedMovementEntity.getId().toString(), movement.id());
                    Assertions.assertEquals(savedMovementEntity.getAccountId().toString(), movement.account_id());
                    Assertions.assertEquals(savedMovementEntity.getType(), movement.type());
                    Assertions.assertEquals(savedMovementEntity.getAmount(), movement.amount());
                    Assertions.assertEquals(savedMovementEntity.getDescription(), movement.description());
                })
                .verifyComplete();
    }

    @Test
    void withdrawWithLimitSuccess() {
        // Arrange
        final var accountId = UUID.randomUUID();
        final var depositDate = LocalDateTime.now();
        final var amount = BigDecimal.TEN;

        final var accountEntity = new AccountEntity(
                accountId,
                UUID.randomUUID().toString(),
                BigDecimal.ZERO,
                BigDecimal.TEN
        );
        Mockito.when(accountRepository.findById(eq(accountId.toString())))
                .thenReturn(Mono.just(accountEntity));

        final var expectedSaveAccount = new AccountEntity(
                accountId,
                accountEntity.getName(),
                BigDecimal.TEN.negate(),
                accountEntity.getLimit()
        );
        Mockito.when(accountRepository.save(eq(expectedSaveAccount)))
                .thenReturn(Mono.just(expectedSaveAccount));

        final var expectedMovement = new MovementEntity(
                null,
                accountId,
                WITHDRAW_DESCRIPTION,
                amount,
                MovementType.DEBIT,
                depositDate
        );
        final var savedMovementEntity = new MovementEntity(
                UUID.randomUUID(),
                expectedMovement.getAccountId(),
                expectedMovement.getDescription(),
                expectedMovement.getAmount(),
                expectedMovement.getType(),
                expectedMovement.getDate()
        );
        Mockito.when(movementRepository.save(eq(expectedMovement)))
                .thenReturn(Mono.just(savedMovementEntity));

        Mockito.when(movementMapper.toMovement(any())).thenCallRealMethod();

        // act and assert
        StepVerifier.create(movementR2DBCAdapter.withdraw(accountId.toString(), amount, depositDate))
                .assertNext(movement -> {
                    Assertions.assertEquals(savedMovementEntity.getId(), UUID.fromString(movement.id()));
                    Assertions.assertEquals(savedMovementEntity.getAccountId(), UUID.fromString(movement.account_id()));
                    Assertions.assertEquals(savedMovementEntity.getType(), movement.type());
                    Assertions.assertEquals(savedMovementEntity.getAmount(), movement.amount());
                    Assertions.assertEquals(savedMovementEntity.getDescription(), movement.description());
                })
                .verifyComplete();
    }

    @Test
    void withdrawWithoutBalanceOrLimitError() {
        // Arrange
        final var accountId = UUID.randomUUID();
        final var depositDate = LocalDateTime.now();
        final var amount = BigDecimal.TEN;

        final var accountEntity = new AccountEntity(
                accountId,
                UUID.randomUUID().toString(),
                BigDecimal.ONE,
                BigDecimal.ZERO
        );
        Mockito.when(accountRepository.findById(eq(accountId.toString())))
                .thenReturn(Mono.just(accountEntity));


        // act and assert
        StepVerifier.create(movementR2DBCAdapter.withdraw(accountId.toString(), amount, depositDate))
                .expectError(InsufficientBalanceException.class)
                .verify();
    }

    @Test
    void withdrawWithAccountNotFound() {
        // Arrange
        final var accountId = UUID.randomUUID().toString();
        final var depositDate = LocalDateTime.now();
        final var amount = BigDecimal.TEN;

        Mockito.when(accountRepository.findById(eq(accountId)))
                .thenReturn(Mono.empty());

        // act and assert
        StepVerifier.create(movementR2DBCAdapter.withdraw(accountId, amount, depositDate))
                .expectError(AccountNotFoundException.class)
                .verify();
    }
}