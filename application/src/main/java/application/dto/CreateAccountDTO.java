package application.dto;


import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateAccountDTO(@NotNull(message = "name can not be empty") String name, BigDecimal limit) {
}
