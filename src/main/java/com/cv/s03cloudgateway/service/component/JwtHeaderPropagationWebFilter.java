package com.cv.s03cloudgateway.service.component;

import com.cv.s03cloudgateway.config.props.CloudGatewayProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Map;

@AllArgsConstructor
@Slf4j
public class JwtHeaderPropagationWebFilter implements WebFilter {

    private final CloudGatewayProperties properties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        log.info("🔐 JwtHeaderPropagationWebFilter - uri: {} method: {}", exchange.getRequest().getURI(), exchange.getRequest().getMethod());
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            // log.info("JwtHeaderPropagationWebFilter.filter Options forward");
            return chain.filter(exchange); // skip JWT propagation
        }
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(auth -> {
                    // log.info("JwtHeaderPropagationWebFilter.filter {}", auth);
                    if (auth != null && auth.isAuthenticated()) {
                        Map<String, Object> principal = (Map<String, Object>) auth.getPrincipal();
                        String bearerToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                .headers(httpHeaders -> {
                                    // Static headers
                                    // httpHeaders.set(HttpHeaders.AUTHORIZATION, bearerToken);
                                    // Dynamic headers from config
                                    for (String header : properties.getAllowedHeaders()) {
                                        if (principal.containsKey(header)) {
                                            String value = String.valueOf(principal.get(header));
                                            if (value != null) {
                                                httpHeaders.set(header, value);
                                            }
                                        }
                                    }
                                }).build();

                        ServerWebExchange mutatedExchange = exchange.mutate()
                                .request(mutatedRequest)
                                .build();
                        return chain.filter(mutatedExchange);
                    }
                    return chain.filter(exchange);
                }).switchIfEmpty(chain.filter(exchange));
    }
}
