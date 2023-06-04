package domain.service;

import domain.model.Movement;
import domain.model.MovementType;
import domain.ports.persistence.MovementPersistencePort;
import domain.ports.service.MovementServicePort;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class MovementServiceImpl implements MovementServicePort {

    private final MovementPersistencePort movementPersistencePort;

    public MovementServiceImpl(final MovementPersistencePort movementPersistencePort) {
        this.movementPersistencePort = movementPersistencePort;
    }

    @Override
    public Mono<Movement> deposit(final String accountId, final BigDecimal amount) {
        return movementPersistencePort.deposit(accountId, amount, LocalDateTime.now());
    }

    @Override
    public Mono<Movement> withdraw(final String accountId, final BigDecimal amount) {
        return movementPersistencePort.withdraw(accountId, amount, LocalDateTime.now());
    }

    @Override
    public Mono<Movement> transfer(final String debitAccount, final String creditAccount, final BigDecimal amount) {
        return movementPersistencePort.transfer(debitAccount, creditAccount, amount, LocalDateTime.now());
    }

    @Override
    public Flux<Movement> findMovementsByPeriod(final String accountId, final LocalDateTime start, final LocalDateTime end, final int page, final int pageSize) {
        return movementPersistencePort.findMovementsByPeriod(accountId, start, end, page, pageSize);
    }

    @Override
    public Flux<Movement> findMovementsByType(final String accountId, final MovementType type, final int page, final int pageSize) {
        return movementPersistencePort.findMovementsByType(accountId, type, page, pageSize);
    }

}
