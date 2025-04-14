package com.cv.s03cloudgateway.config;

import com.cv.s03cloudgateway.config.props.CloudGatewayProperties;
import com.cv.s03cloudgateway.service.component.JwtAuthenticationManager;
import com.cv.s03cloudgateway.service.component.JwtSecurityContextRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    private CloudGatewayProperties properties;

    private final JwtAuthenticationManager authenticationManager;
    private final JwtSecurityContextRepository securityContextRepository;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults())
                .securityContextRepository(securityContextRepository)
                .authenticationManager(authenticationManager)
                .headers(headers -> headers
                        .contentSecurityPolicy(csp ->
                                csp.policyDirectives("default-src 'self'; script-src 'self'; object-src 'none'")
                        )
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.NO_REFERRER))
                        .permissionsPolicy(policy -> policy.policy("geolocation=(), microphone=()"))
                        .frameOptions(ServerHttpSecurity.HeaderSpec.FrameOptionsSpec::disable)
                )
                .authorizeExchange(auth -> auth
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .pathMatchers(properties.getUnauthenticatedPaths().toArray(new String[0])).permitAll()
                        .anyExchange().authenticated()
                ).build();
    }

    // ✅ Proper WebFlux CORS setup
    @Bean
    public CorsWebFilter corsWebFilter() {
        // log.info("CorsWebFilter AllowedOrigins: {}", properties.getAllowedOrigins());
        CorsConfiguration config = new CorsConfiguration();
        // ⚠️ Use specific origins in production
        config.setAllowedOrigins(properties.getAllowedOrigins());
        config.setAllowedMethods(List.of(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
        ));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
