package launcher.config

import domain.ports.persistence.MovementPersistencePort
import domain.ports.service.MovementServicePort
import domain.service.MovementServiceImpl
import org.springframework.context.annotation.{Bean, Configuration}

@Configuration class MovementConfig {
  @Bean def movementPersistencePort(
      movementPersistencePort: MovementPersistencePort
  ) = new MovementServiceImpl(movementPersistencePort)
}
