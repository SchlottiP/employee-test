package de.proeller.applications.employeetest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        authorize -> authorize
                                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/employees/**").authenticated()
                                .requestMatchers(HttpMethod.PUT, "/api/employees/**").authenticated()
                                .requestMatchers(HttpMethod.DELETE, "/api/employees/**").authenticated()
                                .requestMatchers(HttpMethod.GET, "/api/employees/**").permitAll()

                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}