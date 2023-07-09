package com.poc.user.filters;

import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(1)
public class HeaderPropagationFilter implements WebFilter {
    @Override
    @NonNull
    public Mono<Void> filter(ServerWebExchange serverWebExchange,
                             WebFilterChain webFilterChain) {
        String dummyHeaderValue = serverWebExchange
                .getRequest()
                .getHeaders()
                .getFirst("dummy");

        serverWebExchange.getResponse()
                .getHeaders().add("dummy", dummyHeaderValue);
        return webFilterChain.filter(serverWebExchange);
    }
}

