package com.bloodify.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // CSRF is checked only for unsafe methods on these paths, so the
        // existing public POST endpoints (e.g. /api/predict) keep working.
        RequestMatcher unsafeMethod = request -> {
            String m = request.getMethod();
            return !(m.equals("GET") || m.equals("HEAD") || m.equals("OPTIONS") || m.equals("TRACE"));
        };
        RequestMatcher protectedPaths = new OrRequestMatcher(
                antMatcher("/login"),
                antMatcher("/logout"),
                antMatcher("/api/donors/**"),
                antMatcher("/api/admin/**"));
        RequestMatcher csrfMatcher = request ->
                unsafeMethod.matches(request) && protectedPaths.matches(request);

        http
            .csrf(csrf -> csrf.requireCsrfProtectionMatcher(csrfMatcher))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/staff/**", "/api/donors/**").hasAnyRole("ADMIN", "STAFF")
                .anyRequest().permitAll())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/staff/donors", true)
                .failureUrl("/login?error")
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll())
            .exceptionHandling(ex -> ex
                // API calls get 401 instead of a redirect to the login page.
                .defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        antMatcher("/api/**")));

        return http.build();
    }
}