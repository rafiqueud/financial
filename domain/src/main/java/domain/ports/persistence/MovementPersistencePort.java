package domain.ports.persistence;

import domain.model.Movement;
import domain.model.MovementType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MovementPersistencePort {

    Mono<Movement> deposit(final String accountId, final BigDecimal amount, final LocalDateTime date);

    Mono<Movement> withdraw(final String accountId, final BigDecimal amount, final LocalDateTime date);

    Mono<Movement> transfer(final String debitAccount, final String creditAccount, final BigDecimal amount, final LocalDateTime date);

    Flux<Movement> findMovementsByPeriod(final String accountId, final LocalDateTime start, final LocalDateTime end, final int page, final int pageSize);

    Flux<Movement> findMovementsByType(final String accountId, final MovementType type, final int page, final int pageSize);

}
