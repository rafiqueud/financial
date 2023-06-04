package persistence.mappers;

import domain.model.Movement;
import persistence.entity.MovementEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MovementMapper {

    default Movement toMovement(final MovementEntity movementEntity) {
        return new Movement(
                movementEntity.getId().toString(),
                movementEntity.getAccountId().toString(),
                movementEntity.getDescription(),
                movementEntity.getAmount(),
                movementEntity.getType(),
                movementEntity.getDate()
        );
    }
}
