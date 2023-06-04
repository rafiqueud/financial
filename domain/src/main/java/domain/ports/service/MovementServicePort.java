package domain.ports.service;

import domain.model.Movement;
import domain.model.MovementType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MovementServicePort {

    Mono<Movement> deposit(final String accountId, final BigDecimal amount);

    Mono<Movement> withdraw(final String accountId, final BigDecimal amount);

    Mono<Movement> transfer(final String debitAccount, final String creditAccount, final BigDecimal amount);

    Flux<Movement> findMovementsByPeriod(final String accountId, final LocalDateTime start, final LocalDateTime end, final int page, final int pageSize);

    Flux<Movement> findMovementsByType(final String accountId, final MovementType type, final int page, final int pageSize);
}
