package com.andres.curso.springboot.app.springbootcrud.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.andres.curso.springboot.app.springbootcrud.security.filter.JwtAuthenticationFilter;
import com.andres.curso.springboot.app.springbootcrud.security.filter.JwtValidationFilter;

import java.util.Arrays;

// Indicamos que esta clase es una clase de configuración de Spring
@Configuration
// Habilitamos la seguridad a nivel de método, permitiendo el uso de anotaciones como @PreAuthorize
@EnableMethodSecurity(prePostEnabled = true)
public class SpringSecurityConfig {
    
    // Inyectamos la configuración de autenticación proporcionada por Spring Security
    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;

    // Definimos un bean que proporciona el AuthenticationManager, necesario para autenticar usuarios
    @Bean
    AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Definimos la cadena de filtros de seguridad para manejar las peticiones HTTP
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // Configuramos qué rutas están permitidas sin autenticación
                .authorizeHttpRequests((authz) -> authz
                    .requestMatchers(HttpMethod.GET, "/api/users").permitAll() // Permitir GET a /api/users sin autenticación
                    .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll() // Permitir POST a /api/users/register
                    .anyRequest().authenticated()) // Todas las demás rutas requieren autenticación
                // Añadimos el filtro personalizado para autenticación con JWT
                .addFilter(new JwtAuthenticationFilter(authenticationManager()))
                // Añadimos el filtro personalizado para validar tokens JWT en cada petición
                .addFilter(new JwtValidationFilter(authenticationManager()))
                // Desactivamos CSRF porque estamos trabajando con API REST (sin formularios)
                .csrf(config -> config.disable())
                // Habilitamos CORS y definimos la configuración desde un método
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Configuramos la política de sesión como STATELESS (sin sesiones en el servidor)
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Finalmente construimos el objeto SecurityFilterChain
                .build();
    }

    // Configuración del CORS (Cross-Origin Resource Sharing)
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList("*")); // Permitir solicitudes de cualquier origen
        config.setAllowedMethods(Arrays.asList("GET", "POST", "DELETE", "PUT")); // Métodos permitidos
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type")); // Cabeceras permitidas
        config.setAllowCredentials(true); // Permitir el uso de credenciales (cookies, tokens, etc.)

        // Registramos esta configuración para todas las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // Registramos el filtro de CORS con la prioridad más alta
    @Bean
    FilterRegistrationBean<CorsFilter> corsFilter() {
        FilterRegistrationBean<CorsFilter> corsBean = new FilterRegistrationBean<>(
                new CorsFilter(corsConfigurationSource()));
        corsBean.setOrder(Ordered.HIGHEST_PRECEDENCE); // Establece la prioridad más alta
        return corsBean;
    }
}
