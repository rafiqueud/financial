package application.controller;

import application.dto.BalanceDTO;
import application.dto.CreateAccountDTO;
import domain.model.Account;
import domain.ports.service.AccountServicePort;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountServicePort accountServicePort;

    public AccountController(final AccountServicePort accountServicePort) {
        this.accountServicePort = accountServicePort;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Account> createNewAccount(@RequestBody @Valid CreateAccountDTO createAccountDTO) {
        return accountServicePort.createNewAccount(createAccountDTO.name(), createAccountDTO.limit());
    }

    @GetMapping(value = "/{accountId}")
    public Mono<Account> getAccountById(@PathVariable String accountId) {
        return accountServicePort.findAccountById(accountId);
    }

    @GetMapping(value = "/{accountId}/balance")
    public Mono<BalanceDTO> getBalanceById(@PathVariable String accountId) {
        return accountServicePort.retrieveBalanceFromAccountId(accountId)
                .map(BalanceDTO::new);
    }


}
