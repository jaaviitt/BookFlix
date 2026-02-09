package com.trabajoFinal.bookReviews.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        // 1. Permitir acceso a recursos estáticos (CSS, Imágenes, Uploads) y páginas públicas
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/uploads/**").permitAll()
                        .requestMatchers("/registro", "/login").permitAll()
                        .anyRequest().authenticated() // Todo lo demás requiere login
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login") // La ruta donde se envía el formulario (POST)
                        .defaultSuccessUrl("/", true) // A dónde ir si el login es correcto
                        .permitAll()
                )
                .logout((logout) -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout") // Al salir, volvemos al login
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }
}
