package com.claritycheck.Backend.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class OAuthConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        http
                .authorizeHttpRequests(auth -> auth
                        // Simply use a string pattern - Spring Security handles it internally
                        .requestMatchers("/api/chat").permitAll()

                        // Require authentication for all other routes
                        .anyRequest().authenticated()
                )
                .oauth2Login(Customizer.withDefaults())
                .formLogin(Customizer.withDefaults());

        return http.build();
    }
}