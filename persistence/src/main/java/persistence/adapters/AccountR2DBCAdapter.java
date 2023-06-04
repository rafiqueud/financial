package persistence.adapters;

import domain.model.Account;
import domain.ports.persistence.AccountPersistencePort;
import persistence.entity.AccountEntity;
import persistence.mappers.AccountMapper;
import persistence.repository.AccountRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AccountR2DBCAdapter implements AccountPersistencePort {

    private final AccountRepository accountRepository;

    private final AccountMapper accountMapper;

    public AccountR2DBCAdapter(final AccountRepository accountRepository, final AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
    }

    @Override
    public Mono<Account> createNewAccount(final Account account) {
        return accountRepository
                .save(accountMapper.toAccountEntity(account))
                .map(accountMapper::toAccount);
    }

    @Override
    public Mono<Account> findAccountById(String id) {
        return accountRepository.findById(id)
                .map(accountMapper::toAccount);
    }

    @Override
    public Mono<BigDecimal> findBalanceByAccountId(String accountId) {
        return accountRepository.findById(accountId)
                .map(AccountEntity::getBalance);
    }
}
