package com.cv.s03cloudgateway.service.component;

import com.cv.s03cloudgateway.constant.GatewayConstant;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Component
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JWTComponent jwtComponent;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();
        if (!jwtComponent.isTokenValid(authToken)) {
            return Mono.empty();
        } else {
            Claims claims = jwtComponent.extractAllClaims(authToken);
            Map<String, Object> principal = new HashMap<>();
            principal.put(GatewayConstant.PRINCIPAL_USER_ID, claims.getSubject());
            principal.put(GatewayConstant.PRINCIPAL_ID, claims.get(GatewayConstant.PRINCIPAL_ID, String.class));
            principal.put(GatewayConstant.PRINCIPAL_NAME, claims.get(GatewayConstant.PRINCIPAL_NAME, String.class));
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, null);
            return Mono.just(auth);
        }
    }
}
