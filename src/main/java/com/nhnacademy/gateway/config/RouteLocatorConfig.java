package com.nhnacademy.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteLocatorConfig {

    @Bean
    public RouteLocator myRoute(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("account-api", r -> r.path("/accounts/**")
                        .uri("http://localhost:8081"))
                
                .route("task-api", r -> r.path("/projects/**")
                        .uri("http://localhost:8082"))
                .build();
    }
}
