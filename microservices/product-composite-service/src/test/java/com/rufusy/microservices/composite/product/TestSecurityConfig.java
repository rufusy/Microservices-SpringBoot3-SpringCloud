package com.rufusy.microservices.composite.product;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        // Disable OAuth machinery from kicking in when are running integration tests
        http
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchange -> exchange.anyExchange().permitAll());

        return http.build();
    }
}
