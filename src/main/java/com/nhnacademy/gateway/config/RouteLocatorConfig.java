package com.nhnacademy.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;

public class RouteLocatorConfig {
    public RouteLocator myRoute(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("account_api", r -> r.path("/accounts/**")
                        .uri("http://localhost:8081"))

                .route("task_api", r -> r.path("/projects/**")
                        .uri("http://localhost:8082"))
                .build();
    }
}
