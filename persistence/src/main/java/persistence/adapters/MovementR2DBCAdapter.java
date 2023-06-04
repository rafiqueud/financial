package persistence.adapters;

import domain.exception.AccountNotFoundException;
import domain.exception.InsufficientBalanceException;
import domain.model.Movement;
import domain.model.MovementType;
import domain.ports.persistence.MovementPersistencePort;
import persistence.entity.AccountEntity;
import persistence.entity.MovementEntity;
import persistence.mappers.MovementMapper;
import persistence.repository.AccountRepository;
import persistence.repository.MovementRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

import static domain.model.MovementDescription.CREDIT_TRANSFER_DESCRIPTION;
import static domain.model.MovementDescription.DEBIT_TRANSFER_DESCRIPTION;
import static domain.model.MovementDescription.DEPOSIT_DESCRIPTION;
import static domain.model.MovementDescription.WITHDRAW_DESCRIPTION;

@Service
public class MovementR2DBCAdapter implements MovementPersistencePort {

    private final AccountRepository accountRepository;

    private final MovementRepository movementRepository;

    private final MovementMapper movementMapper;


    public MovementR2DBCAdapter(final AccountRepository accountRepository,
                                final MovementRepository movementRepository,
                                final MovementMapper movementMapper) {
        this.accountRepository = accountRepository;
        this.movementRepository = movementRepository;
        this.movementMapper = movementMapper;
    }

    @Override
    @Transactional
    public Mono<Movement> deposit(final String accountId, final BigDecimal amount, final LocalDateTime date) {
        return accountRepository.findById(accountId)
                .switchIfEmpty(Mono.error(new AccountNotFoundException(accountId)))
                .flatMap(accountEntity -> deposit(accountEntity, amount, date));
    }

    @Override
    @Transactional
    public Mono<Movement> withdraw(final String accountId, final BigDecimal amount, final LocalDateTime date) {
        return accountRepository.findById(accountId)
                .switchIfEmpty(Mono.error(new AccountNotFoundException(accountId)))
                .flatMap(accountEntity -> withdraw(accountEntity, amount, date));
    }

    @Override
    @Transactional
    public Mono<Movement> transfer(final String debitAccountId, final String creditAccountId, final BigDecimal amount, final LocalDateTime date) {
        final var debitAccountMono = accountRepository.findById(debitAccountId)
                .switchIfEmpty(Mono.error(new AccountNotFoundException(debitAccountId)));

        final var creditAccountMono = accountRepository.findById(creditAccountId)
                .switchIfEmpty(Mono.error(new AccountNotFoundException(creditAccountId)));

        return Mono.zip(debitAccountMono, creditAccountMono)
                .flatMap(accounts -> {
                    final var debitAccount = accounts.getT1();
                    final var creditAccount = accounts.getT2();
                    validateBalance(debitAccount, amount);
                    return transfer(debitAccount, creditAccount, amount, date);
                });
    }

    @Override
    public Flux<Movement> findMovementsByPeriod(final String accountId, final LocalDateTime start, final LocalDateTime end, final int page, final int pageSize) {
        final var pageable = PageRequest.of(page, pageSize, Sort.by("date").descending());
        return movementRepository.findAllByAccountIdAndDateIsBetween(accountId, start, end, pageable)
                .map(movementMapper::toMovement);
    }

    @Override
    public Flux<Movement> findMovementsByType(final String accountId, final MovementType type, final int page, final int pageSize) {
        final var pageable = PageRequest.of(page, pageSize, Sort.by("date").descending());
        return movementRepository.findAllByAccountIdAndType(accountId, type, pageable)
                .map(movementMapper::toMovement);
    }

    private Mono<Movement> transfer(final AccountEntity debitAccount, final AccountEntity creditAccount, final BigDecimal amount, final LocalDateTime date) {
        debitAccount.setBalance(debitAccount.getBalance().subtract(amount));
        final var debitMovementEntity = new MovementEntity(
                null,
                debitAccount.getId(),
                String.format(DEBIT_TRANSFER_DESCRIPTION, creditAccount.getId()),
                amount,
                MovementType.DEBIT,
                date
        );

        creditAccount.setBalance(creditAccount.getBalance().add(amount));
        final var creditMovementEntity = new MovementEntity(
                null,
                creditAccount.getId(),
                String.format(CREDIT_TRANSFER_DESCRIPTION, debitAccount.getId()),
                amount,
                MovementType.CREDIT,
                date
        );

        final var debitAccountSaveMono = accountRepository.save(debitAccount);
        final var debitMovementSaveMono = movementRepository.save(debitMovementEntity);

        final var creditAccountSaveMono = accountRepository.save(creditAccount);
        final var creditMovementSaveMono = movementRepository.save(creditMovementEntity);

        return Mono.zip(debitAccountSaveMono, creditAccountSaveMono, debitMovementSaveMono, creditMovementSaveMono)
                .map(Tuple3::getT3)
                .map(movementMapper::toMovement);
    }

    private Mono<Movement> withdraw(final AccountEntity account, final BigDecimal amount, final LocalDateTime date) {
        validateBalance(account, amount);
        account.setBalance(account.getBalance().subtract(amount));
        final var accountSaveMono = accountRepository.save(account);

        final var movementEntity = new MovementEntity(
                null,
                account.getId(),
                WITHDRAW_DESCRIPTION,
                amount,
                MovementType.DEBIT,
                date
        );

        final var movementSaveMono = movementRepository.save(movementEntity);
        return Mono.zip(accountSaveMono, movementSaveMono)
                .map(Tuple2::getT2)
                .map(movementMapper::toMovement);
    }

    private void validateBalance(final AccountEntity account, final BigDecimal amountWithdraw) {
        if (account.getBalance().add(account.getLimit()).compareTo(amountWithdraw) < 0) {
            throw new InsufficientBalanceException();
        }
    }

    private Mono<Movement> deposit(final AccountEntity account, final BigDecimal amount, final LocalDateTime date) {
        account.setBalance(account.getBalance().add(amount));
        final var accountSaveMono = accountRepository.save(account);

        final var movementEntity = new MovementEntity(
                null,
                account.getId(),
                DEPOSIT_DESCRIPTION,
                amount,
                MovementType.CREDIT,
                date
        );
        final var movementSaveMono = movementRepository.save(movementEntity);
        return Mono.zip(accountSaveMono, movementSaveMono)
                .map(Tuple2::getT2)
                .map(movementMapper::toMovement);
    }

}
