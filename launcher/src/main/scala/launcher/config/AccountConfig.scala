package launcher.config

import domain.ports.persistence.AccountPersistencePort
import domain.ports.service.AccountServicePort
import domain.service.AccountServiceImpl
import org.springframework.context.annotation.{Bean, Configuration}

@Configuration class AccountConfig {
  @Bean def accountServicePort(accountPersistencePort: AccountPersistencePort) =
    new AccountServiceImpl(accountPersistencePort)
}
