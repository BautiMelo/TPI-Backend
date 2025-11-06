package com.backend.tpi.api_gateway.filters;

import com.backend.tpi.api_gateway.security.GatewayTokenService;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GatewayAuthGlobalFilter implements GlobalFilter {

    private final GatewayTokenService tokenService;

    public GatewayAuthGlobalFilter(GatewayTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Obtain token (blocking). It's fast due to caching. If you prefer non-blocking, adapt TokenService to reactive.
        String token = tokenService.getAccessToken();
        if (token != null) {
            exchange.getRequest().mutate()
                    .header("Authorization", "Bearer " + token)
                    .build();
        }
        return chain.filter(exchange);
    }
}
