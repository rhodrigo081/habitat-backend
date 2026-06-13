package org.example.config;

import lombok.RequiredArgsConstructor;
import org.example.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()

                        .requestMatchers(HttpMethod.POST, "/users/**").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/users/**").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/users/**").hasAnyRole("ADMINISTRATOR", "COORDINATOR", "INTERN")

                        .requestMatchers(HttpMethod.GET, "/associados/**")
                        .hasAnyRole("ADMINISTRATOR", "COORDINATOR", "INTERN")
                        .requestMatchers(HttpMethod.GET, "/consultations/**")
                        .hasAnyRole("ADMINISTRATOR", "COORDINATOR", "INTERN")
                        .requestMatchers(HttpMethod.GET, "/processes/**")
                        .hasAnyRole("ADMINISTRATOR", "COORDINATOR", "INTERN")
                        .requestMatchers(HttpMethod.GET, "/conciliations/**")
                        .hasAnyRole("ADMINISTRATOR", "COORDINATOR", "INTERN")

                        .requestMatchers(HttpMethod.POST, "/associados/**")
                        .hasAnyRole("INTERN", "ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/associados/**")
                        .hasAnyRole("INTERN", "ADMINISTRATOR")
                        .requestMatchers(HttpMethod.POST, "/consultations/**")
                        .hasAnyRole("INTERN", "ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/consultations/**")
                        .hasAnyRole("INTERN", "ADMINISTRATOR")
                        .requestMatchers(HttpMethod.POST, "/processes/**")
                        .hasAnyRole("INTERN", "ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/processes/**")
                        .hasAnyRole("INTERN", "ADMINISTRATOR")
                        .requestMatchers(HttpMethod.POST, "/conciliations/**")
                        .hasAnyRole("INTERN", "ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/conciliations/**")
                        .hasAnyRole("INTERN", "ADMINISTRATOR")

                        .requestMatchers("/documents/**")
                        .hasAnyRole("INTERN", "COORDINATOR", "ADMINISTRATOR")

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
