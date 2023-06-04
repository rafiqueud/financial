package persistence.mappers;

import domain.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import persistence.entity.AccountEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountMapper {

    AccountEntity toAccountEntity(final Account account);

    Account toAccount(final AccountEntity accountEntity);

}
