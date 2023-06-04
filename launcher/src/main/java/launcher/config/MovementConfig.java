package launcher.config;

import domain.ports.persistence.MovementPersistencePort;
import domain.ports.service.MovementServicePort;
import domain.service.MovementServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MovementConfig {

    @Bean
    public MovementServicePort movementPersistencePort(final MovementPersistencePort movementPersistencePort) {
        return new MovementServiceImpl(movementPersistencePort);
    }

}
