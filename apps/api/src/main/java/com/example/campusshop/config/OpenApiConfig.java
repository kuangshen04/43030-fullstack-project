package com.example.campusshop.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI campusShopOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Campus Shop API")
                .version("1.0.0")
                .description("""
                        A small product catalog API used for a full-stack teaching demo.
                        The OpenAPI document is the source of truth for the frontend API types.
                        """));
    }
}
