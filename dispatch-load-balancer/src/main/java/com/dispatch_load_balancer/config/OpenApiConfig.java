package com.dispatch_load_balancer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI dispatchLoadBalancerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Dispatch Load Balancer API")
                        .description(
                                "REST API for managing delivery orders, vehicles, " +
                                        "and generating optimized dispatch plans."
                        )
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Dispatch Load Balancer")));
    }
}