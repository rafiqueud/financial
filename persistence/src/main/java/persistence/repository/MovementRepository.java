package persistence.repository;

import domain.model.MovementType;
import persistence.entity.MovementEntity;
import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MovementRepository extends R2dbcRepository<MovementEntity, String> {

    Flux<MovementEntity> findAllByAccountIdAndDateIsBetween(final String accountId, final LocalDateTime start, final LocalDateTime end, final Pageable pageable);

    Flux<MovementEntity> findAllByAccountIdAndType(final String accountId, final MovementType type, final Pageable pageable);
}
