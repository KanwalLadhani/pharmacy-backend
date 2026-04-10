package com.pharmacy.pharmacy_system.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.pharmacy.pharmacy_system.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthEntryPoint authEntryPoint;
    
    @Value("${FRONTEND_URL:http://localhost:5173}")
    private String frontendUrl;


    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          CustomAccessDeniedHandler accessDeniedHandler,
                          CustomAuthEntryPoint authEntryPoint) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accessDeniedHandler = accessDeniedHandler;
        this.authEntryPoint = authEntryPoint;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) {
                return userRepository.findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException(
                                "User not found with username: " + username
                        ));
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable) // Disabled here because we use the CorsFilter Bean instead
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/", "/api/auth/**").permitAll()

                // Admin-only: mutating medicine data and clearing sales
                .requestMatchers(HttpMethod.POST,   "/api/medicines/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/medicines/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/medicines/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/billing/sales/clear").hasRole("ADMIN")

                // Admin-only: user management
                .requestMatchers("/api/users/**").hasRole("ADMIN")

                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // Use patterns to allow any origin while supporting credentials
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedHeaders(List.of("Origin", "Content-Type", "Accept", "Authorization", "X-Requested-With"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
