// Paquete donde se encuentra el filtro
package com.andres.curso.springboot.app.springbootcrud.security.filter;

// Importación de clases necesarias para el funcionamiento del filtro
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.andres.curso.springboot.app.springbootcrud.entities.User;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static com.andres.curso.springboot.app.springbootcrud.security.TokenJwtConfig.*;

// Filtro que intercepta las peticiones de autenticación y genera un JWT si la autenticación es exitosa
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;

    // Constructor que recibe el AuthenticationManager para validar usuarios
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    // Método que intenta autenticar al usuario con los datos del request
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        User user = null;
        String username = null;
        String password = null;

        try {
            // Se obtiene el usuario desde el cuerpo del request (JSON -> Objeto Java)
            user = new ObjectMapper().readValue(request.getInputStream(), User.class);
            username = user.getUsername();
            password = user.getPassword();
        } catch (StreamReadException e) {
            e.printStackTrace();
        } catch (DatabindException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Se crea el token con el username y password para autenticación
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);

        // Se autentica al usuario
        return authenticationManager.authenticate(authenticationToken);
    }

    // Método que se ejecuta si la autenticación fue exitosa
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authResult) throws IOException, ServletException {

        // Obtenemos el usuario autenticado y sus roles
        org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) authResult.getPrincipal();
        String username = user.getUsername();
        Collection<? extends GrantedAuthority> roles = authResult.getAuthorities();

        // Se construyen los claims (datos adicionales en el JWT)
        Claims claims = Jwts.claims()
                .add("authorities", new ObjectMapper().writeValueAsString(roles)) // Se agregan roles como String
                .add("username", username)
                .build();

        // Se construye el JWT con los claims, fecha de expiración, firma, etc.
        String token = Jwts.builder()
                .subject(username) // subject del JWT es el username
                .claims(claims)
                .expiration(new Date(System.currentTimeMillis() + 3600000)) // Expira en 1 hora
                .issuedAt(new Date()) // Fecha de emisión
                .signWith(SECRET_KEY) // Llave secreta para firmar el token
                .compact(); // Genera el token en forma de String

        // Se agrega el token al header de la respuesta
        response.addHeader(HEADER_AUTHORIZATION, PREFIX_TOKEN + token);

        // Se prepara el cuerpo de la respuesta como JSON
        Map<String, String> body = new HashMap<>();
        body.put("token", token);
        body.put("username", username);
        body.put("message", String.format("Hola %s has iniciado sesion con exito!", username));

        // Se escribe el cuerpo como respuesta al cliente
        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
        response.setContentType(CONTENT_TYPE);
        response.setStatus(200);
    }

    // Método que se ejecuta si la autenticación falla
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException failed) throws IOException, ServletException {
        Map<String, String> body = new HashMap<>();
        body.put("message", "Error en la autenticacion username o password incorrectos!");
        body.put("error", failed.getMessage());

        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
        response.setStatus(401); // Unauthorized
        response.setContentType(CONTENT_TYPE);
    }

}
