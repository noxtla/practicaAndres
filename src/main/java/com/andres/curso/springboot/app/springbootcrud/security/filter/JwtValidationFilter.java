// Paquete donde se encuentra la clase
package com.andres.curso.springboot.app.springbootcrud.security.filter;

// Importación de constantes usadas para configuración del token JWT
import static com.andres.curso.springboot.app.springbootcrud.security.TokenJwtConfig.CONTENT_TYPE;
import static com.andres.curso.springboot.app.springbootcrud.security.TokenJwtConfig.HEADER_AUTHORIZATION;
import static com.andres.curso.springboot.app.springbootcrud.security.TokenJwtConfig.PREFIX_TOKEN;
import static com.andres.curso.springboot.app.springbootcrud.security.TokenJwtConfig.SECRET_KEY;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.andres.curso.springboot.app.springbootcrud.security.SimpleGrantedAuthorityJsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro que valida el JWT en cada solicitud para permitir o denegar acceso.
 */
public class JwtValidationFilter extends BasicAuthenticationFilter {

    // Constructor que recibe el AuthenticationManager y lo pasa al padre
    public JwtValidationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    /**
     * Método que intercepta cada petición HTTP para validar el JWT.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Se obtiene el encabezado de autorización
        String header = request.getHeader(HEADER_AUTHORIZATION);

        // Si no hay encabezado o no empieza con el prefijo del token (ej: "Bearer "), se continúa con el siguiente filtro
        if (header == null || !header.startsWith(PREFIX_TOKEN)) {
            chain.doFilter(request, response);
            return;
        }

        // Se remueve el prefijo para obtener solo el token
        String token = header.replace(PREFIX_TOKEN, "");

        try {
            // Se parsea el token y se obtienen los claims (información contenida en el JWT)
            Claims claims = Jwts.parser()
                .verifyWith(SECRET_KEY) // Verifica el token con la clave secreta
                .build()
                .parseSignedClaims(token)
                .getPayload();

            // Se obtiene el nombre de usuario (subject del token)
            String usename = claims.getSubject();

            // Se obtiene la lista de roles/autorizaciones desde el claim "authorities"
            Object authoritiesClaims = claims.get("authorities");

            // Se convierte el claim de autoridades a una colección de objetos GrantedAuthority
            Collection<? extends GrantedAuthority> authorities = Arrays.asList(
                new ObjectMapper()
                    .addMixIn(SimpleGrantedAuthority.class, SimpleGrantedAuthorityJsonCreator.class)
                    .readValue(authoritiesClaims.toString().getBytes(), SimpleGrantedAuthority[].class)
            );

            // Se crea un token de autenticación con el usuario y las autoridades
            UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(usename, null, authorities);

            // Se establece el token en el contexto de seguridad de Spring
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            // Se continúa con la cadena de filtros
            chain.doFilter(request, response);

        } catch (JwtException e) {
            // En caso de token inválido, se genera una respuesta de error
            Map<String, String> body = new HashMap<>();
            body.put("error", e.getMessage());
            body.put("message", "El token JWT es invalido!");

            // Se escribe la respuesta con el mensaje de error
            response.getWriter().write(new ObjectMapper().writeValueAsString(body));
            response.setStatus(HttpStatus.UNAUTHORIZED.value()); // Código 401
            response.setContentType(CONTENT_TYPE);
        }
    }
}
