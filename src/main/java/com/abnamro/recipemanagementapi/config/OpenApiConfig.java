package com.abnamro.recipemanagementapi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Recipe Management API",
                version = "1.0",
                description = "CRUD + filtering API for the assignment"
        )
)
public class OpenApiConfig {
}
