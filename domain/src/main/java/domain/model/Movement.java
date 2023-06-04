package domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Movement(String id, String account_id, String description, BigDecimal amount, MovementType type,
                       LocalDateTime date) {
}
