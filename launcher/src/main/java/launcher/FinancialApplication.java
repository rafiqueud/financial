package launcher;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication(scanBasePackages = {"application", "persistence", "launcher"})
@EnableR2dbcRepositories(basePackages = "persistence")
@OpenAPIDefinition(info = @Info(title = "Financial", version = "1.0", description = "Documentation Financial APIs v1.0"))
public class FinancialApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinancialApplication.class, args);
    }
}
