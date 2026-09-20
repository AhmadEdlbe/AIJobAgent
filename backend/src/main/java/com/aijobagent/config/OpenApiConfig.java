package com.aijobagent.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI aiJobAgentOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI Job Agent Backend API")
                        .description("Personal AI-powered job search assistant. Single-device JWT, Job aggregation from 7+ sources, OpenAI matching, cover letter & interview prep.")
                        .version("1.0.0")
                        .contact(new Contact().name("AI Job Agent").email("support@aijobagent.com")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT from POST /auth/register")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
