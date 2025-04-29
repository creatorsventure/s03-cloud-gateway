package com.cv.s03cloudgateway.service.component;

import com.cv.s03cloudgateway.constant.GatewayConstant;
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

@Slf4j
public class JwtHeaderPropagationWebFilter implements WebFilter {

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
                                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                                .header(GatewayConstant.X_HEADER_USER_ID, (String) principal.get(GatewayConstant.PRINCIPAL_USER_ID))
                                .header(GatewayConstant.X_HEADER_USER_KEY, (String) principal.get(GatewayConstant.PRINCIPAL_ID))
                                .header(GatewayConstant.X_HEADER_USER_NAME, (String) principal.get(GatewayConstant.PRINCIPAL_NAME))
                                .build();

                        ServerWebExchange mutatedExchange = exchange.mutate()
                                .request(mutatedRequest)
                                .build();
                        return chain.filter(mutatedExchange);
                    }
                    return chain.filter(exchange);
                }).switchIfEmpty(chain.filter(exchange));
    }
}
