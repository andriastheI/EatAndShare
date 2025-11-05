package edu.carroll.eatAndShare.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Expose CSRF token on the request as attribute named "_csrf"
        CsrfTokenRequestAttributeHandler csrfAttr = new CsrfTokenRequestAttributeHandler();
        csrfAttr.setCsrfRequestAttributeName("_csrf");

        http
                // CSRF is ENABLED by default; do NOT disable it
                .csrf(csrf -> csrf.csrfTokenRequestHandler(csrfAttr))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}