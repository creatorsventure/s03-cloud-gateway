package com.cv.s03cloudgateway.service.component;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;

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
            return Mono.just(new UsernamePasswordAuthenticationToken(
                    new HashMap<>(jwtComponent.extractAllClaims(authToken)), null, null));
        }
    }
}
