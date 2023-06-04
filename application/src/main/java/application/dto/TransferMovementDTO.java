package application.dto;

import java.math.BigDecimal;

public record TransferMovementDTO(String creditAccountId, BigDecimal amount) {

}
