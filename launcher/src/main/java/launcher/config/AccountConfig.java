package launcher.config;

import domain.ports.persistence.AccountPersistencePort;
import domain.ports.persistence.MovementPersistencePort;
import domain.ports.service.AccountServicePort;
import domain.ports.service.MovementServicePort;
import domain.service.AccountServiceImpl;
import domain.service.MovementServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountConfig {


    @Bean
    public AccountServicePort accountServicePort(final AccountPersistencePort accountPersistencePort) {
        return new AccountServiceImpl(accountPersistencePort);
    }

}
