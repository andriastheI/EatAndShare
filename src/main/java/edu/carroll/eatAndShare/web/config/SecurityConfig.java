package edu.carroll.eatAndShare.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/**
 * Filename: SecurityConfig.java
 * Author: Andrias, Selin
 * Date: October 20, 2025
 *
 * Description:
 * Security configuration for the EatAndShare application. This class defines
 * Spring Security behavior, including CSRF token exposure, request rules,
 * and password encoding strategy. CSRF protection remains enabled, and all
 * HTTP requests are permitted to support this project's open-access design.
 */
@Configuration
public class SecurityConfig {

    /**
     * Configures the Spring Security filter chain.
     *
     * <p>Key behaviors:</p>
     * <ul>
     *   <li>CSRF remains enabled (default)—important for all POST/PUT/DELETE forms.</li>
     *   <li>CSRF token is attached to each request as attribute "_csrf".</li>
     *   <li>All endpoints are permitted without authentication.</li>
     *   <li>Login and logout features are disabled since the application manually
     *       handles user sessions.</li>
     * </ul>
     *
     * @param http Spring Security HTTP configuration object
     * @return a fully built {@link SecurityFilterChain}
     * @throws Exception if Spring Security encounters any configuration error
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // Attach CSRF token to each request under "_csrf"
        CsrfTokenRequestAttributeHandler csrfAttr = new CsrfTokenRequestAttributeHandler();
        csrfAttr.setCsrfRequestAttributeName("_csrf");

        http
                // CSRF ENABLED (default). The token is added to each request.
                .csrf(csrf -> csrf.csrfTokenRequestHandler(csrfAttr))

                // Allow all requests — controllers handle user access logic.
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())

                // Disable Spring Security's login form (we use our own login).
                .formLogin(form -> form.disable())

                // Disable logout mechanism (handled manually in controllers).
                .logout(logout -> logout.disable());

        return http.build();
    }

    /**
     * Provides a password encoder for hashing user passwords using BCrypt.
     *
     * @return a new BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
