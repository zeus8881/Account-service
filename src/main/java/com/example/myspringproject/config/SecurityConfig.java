package com.example.myspringproject.config;

import com.example.myspringproject.exception.AccessDeniedHandlerException;
import com.example.myspringproject.exception.FailureHandler;
import com.example.myspringproject.exception.RestAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class SecurityConfig {
    private final FailureHandler failureHandler;
    private final AccessDeniedHandlerException accessDeniedHandlerException;

    @Autowired
    public SecurityConfig(FailureHandler failureHandler, AccessDeniedHandlerException accessDeniedHandlerException) {
        this.failureHandler = failureHandler;
        this.accessDeniedHandlerException = accessDeniedHandlerException;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(restAuthenticationEntryPoint()))
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable).disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/changepass").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/empl/payments").hasAnyRole("ACCOUNTANT", "USER")
                        .requestMatchers(HttpMethod.POST, "/api/acct/payments").hasAnyRole("ACCOUNTANT", "USER")
                        .requestMatchers(HttpMethod.PUT, "/api/acct/payments").hasRole("ACCOUNTANT")
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/user").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/user/role").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/admin/user").hasRole("ADMINISTRATOR")
                        .requestMatchers("/h2/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.failureHandler(failureHandler))
                .sessionManagement(sessions ->
                        sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(custom -> custom.accessDeniedHandler(accessDeniedHandlerException));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }
}
