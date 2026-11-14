package com.educampus.academic.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Métadonnées de la documentation générée (Swagger UI : /swagger-ui.html). */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI academicOpenApi() {
        return new OpenAPI().info(new Info()
                .title("EduCampus - Academic Service API")
                .description("API REST du service Academic : étudiants et cours (versionnement /api/v1).")
                .version("1.0.0"));
    }
}
