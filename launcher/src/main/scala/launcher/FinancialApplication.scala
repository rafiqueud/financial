package launcher

import io.swagger.v3.oas.annotations.info.{Contact, Info, License}
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.{OpenAPIDefinition, info}
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication(scanBasePackages =
  Array("application", "persistence", "launcher")
)
@EnableR2dbcRepositories(basePackages = Array("persistence"))
@OpenAPIDefinition(
  info = new Info(
    title = "Financial",
    version = "1.0",
    description = "Documentation Financial APIs v1.0"
  )
)
class FinancialApplication

@main
def main(): Unit = SpringApplication.run(classOf[FinancialApplication])
