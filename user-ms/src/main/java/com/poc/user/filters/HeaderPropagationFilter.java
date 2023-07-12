package com.poc.user.filters;

import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(2)
public class HeaderPropagationFilter implements WebFilter {
    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange serverWebExchange,
                             @NonNull WebFilterChain webFilterChain) {
       return Mono.deferContextual(ctx -> {
            serverWebExchange.getResponse()
                    .getHeaders().add("dummy", ctx.get("dummy"));
            return webFilterChain.filter(serverWebExchange);
        });
    }
}

