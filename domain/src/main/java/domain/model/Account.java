package domain.model;

import java.math.BigDecimal;

public record Account(String id, String name, BigDecimal balance, BigDecimal limit) {
}
