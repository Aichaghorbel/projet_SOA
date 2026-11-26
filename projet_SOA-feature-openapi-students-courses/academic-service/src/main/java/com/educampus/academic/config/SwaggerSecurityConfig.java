package com.educampus.academic.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Ajoute le bouton Authorize (JWT Bearer) dans Swagger UI. Ne change pas le comportement de l'API. */
@Configuration
public class SwaggerSecurityConfig {

    @Bean
    public OpenApiCustomizer bearerAuthCustomizer() {
        return openApi -> {
            Components components = openApi.getComponents() == null ? new Components() : openApi.getComponents();
            components.addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT"));
            openApi.setComponents(components);
            openApi.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
        };
    }
}