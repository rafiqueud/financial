package application.controller;

import application.dto.CreateMovementDTO;
import application.dto.TransferMovementDTO;
import domain.model.Movement;
import domain.model.MovementType;
import domain.ports.service.MovementServicePort;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/movements/{accountId}")
public class MovementController {

    private final MovementServicePort movementServicePort;

    public MovementController(final MovementServicePort movementServicePort) {
        this.movementServicePort = movementServicePort;
    }

    @PostMapping("/deposit")
    public Mono<Movement> deposit(@PathVariable final String accountId, @RequestBody final CreateMovementDTO createMovementDTO) {
        return movementServicePort.deposit(accountId, createMovementDTO.amount());
    }

    @PostMapping("/withdraw")
    public Mono<Movement> withdraw(@PathVariable final String accountId, @RequestBody final CreateMovementDTO createMovementDTO) {
        return movementServicePort.withdraw(accountId, createMovementDTO.amount());
    }

    @PostMapping("/transfer")
    public Mono<Movement> transfer(@PathVariable final String accountId, @RequestBody final TransferMovementDTO transferMovementDTO) {
        return movementServicePort.transfer(accountId, transferMovementDTO.creditAccountId(), transferMovementDTO.amount());
    }

    @GetMapping
    public Flux<Movement> findMovementsByPeriod(@PathVariable final String accountId,
                                                @RequestParam(value = "start", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                @RequestParam(value = "end", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                @RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
                                                @RequestParam(value = "pageSize", defaultValue = "25", required = false) Integer pageSize
    ) {
        return movementServicePort.findMovementsByPeriod(accountId, startDate.atStartOfDay(), endDate.atStartOfDay().plusDays(1).minusNanos(1), page, pageSize);
    }

    @GetMapping("/credit")
    public Flux<Movement> findCreditMovements(@PathVariable final String accountId,
                                              @RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
                                              @RequestParam(value = "pageSize", defaultValue = "25", required = false) Integer pageSize) {
        return movementServicePort.findMovementsByType(accountId, MovementType.CREDIT, page, pageSize);
    }

    @GetMapping("/debit")
    public Flux<Movement> findDebitMovements(@PathVariable final String accountId,
                                             @RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
                                             @RequestParam(value = "pageSize", defaultValue = "25", required = false) Integer pageSize) {
        return movementServicePort.findMovementsByType(accountId, MovementType.DEBIT, page, pageSize);
    }

}
