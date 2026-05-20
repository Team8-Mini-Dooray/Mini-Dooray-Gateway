package com.nhnacademy.gateway.filter;

import java.util.Base64;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class SessionValidationFilter implements GlobalFilter {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    public SessionValidationFilter(ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (path.startsWith("/accounts/login") || path.startsWith("/accounts/signup")) {
            return chain.filter(exchange);
        }

        HttpCookie sessionCookie = exchange.getRequest().getCookies().getFirst("SESSION");
        if (sessionCookie == null || sessionCookie.getValue().isEmpty()) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String sessionId;
        try {
            sessionId = new String(Base64.getDecoder().decode(sessionCookie.getValue()));
        } catch (IllegalArgumentException e) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String redisKey = "spring:session:sessions:" + sessionId;

        return redisTemplate.opsForHash()
                .get(redisKey, "sessionAttr:USER_ID")
                .switchIfEmpty(Mono.defer(() -> {
                    System.out.println("USER_ID 없음. redisKey: " + redisKey);
                    return Mono.error(new RuntimeException("no session"));
                }))
                .flatMap(userIdObj -> {
                    String userId = userIdObj.toString().replace("\"", "");

                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(req -> req.headers(h -> h.add("X-User-Id", userId)))
                            .build();

                    return chain.filter(mutatedExchange);
                })
                .onErrorResume(e -> onError(exchange, HttpStatus.UNAUTHORIZED));
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}
