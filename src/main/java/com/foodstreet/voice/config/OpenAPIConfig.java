package com.foodstreet.voice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI streetVoiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Street Voice API")
                        .description("Backend API for Food Street Location-based Audio Guide")
                        .version("1.0"));
    }
}
