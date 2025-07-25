package com.example.gl_exercise.configuration;

import com.example.gl_exercise.filter.JwtAuthenticationFilter;
import jakarta.servlet.Filter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
class HttpSecurityConfig {

    private final Filter jwtAuthenticationFilter;

    public HttpSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Filtro de seguridad global para las peticiones http.
     * Deshabilita la protección CSRF y la creación de sesiones http.
     * Autoriza a cualquier usuario a realizar llamadas POST sobre el endpoint /sign-up.
     * Las demás llamadas van a pasar por el filtro JWT.
     *
     * @param http objeto para configurar la seguridad http
     * @return SecurityFilterChain objeto construido y configurado para la cadena de filtros http.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/sign-up").permitAll()
                .anyRequest().authenticated()
            ).addFilterBefore(this.jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

}
