package com.poc.user.filters;

import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Component
@Order(1)
public class HeaderAdditionFilter implements WebFilter {
    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange serverWebExchange,
                             WebFilterChain webFilterChain) {
        return webFilterChain.filter(serverWebExchange)
                .contextWrite(Context.of("dummy", "dummy header exists"));
    }
}
