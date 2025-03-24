// Paquete donde se encuentra esta clase, relacionada con la seguridad del proyecto.
package com.andres.curso.springboot.app.springbootcrud.security;

// Importa la clase SecretKey, utilizada para firmar y validar tokens JWT.
import javax.crypto.SecretKey;

// Importa la clase Jwts del paquete de JWT (io.jsonwebtoken) para construir el secreto.
import io.jsonwebtoken.Jwts;

// Clase de configuración para centralizar constantes relacionadas con JWT.
public class TokenJwtConfig {
    
    // Clave secreta para firmar y validar tokens JWT usando el algoritmo HS256.
    // Se genera dinámicamente al iniciar la aplicación (no es persistente).
    public static final SecretKey SECRET_KEY = Jwts.SIG.HS256.key().build();

    // Prefijo que se utiliza en el encabezado Authorization para indicar que se está usando un token Bearer.
    public static final String PREFIX_TOKEN = "Bearer ";

    // Nombre del encabezado HTTP donde se envía el token JWT.
    public static final String HEADER_AUTHORIZATION = "Authorization";

    // Tipo de contenido esperado o devuelto por las APIs (usualmente JSON).
    public static final String CONTENT_TYPE = "application/json";
}
