package io.github.dmytrozelinskyy.currencyconverter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/cc-console/**"))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("/cc-console/**").permitAll()
                                .requestMatchers("/", "/index.html", "/css/**", "/js/**").permitAll()
                                .requestMatchers("/api/**").permitAll()
                                .anyRequest().authenticated()
                ).httpBasic(basic -> {});
        return http.build();
    }
}
