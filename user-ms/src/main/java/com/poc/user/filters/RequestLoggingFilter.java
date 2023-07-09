package com.poc.user.filters;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;


@Component
@Order(0)
@Log4j2
public class RequestLoggingFilter implements WebFilter {
    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        log.info("incoming request identified as {}",
                () -> serverWebExchange.getRequest().getURI());

        return webFilterChain.filter(serverWebExchange);
    }
}
