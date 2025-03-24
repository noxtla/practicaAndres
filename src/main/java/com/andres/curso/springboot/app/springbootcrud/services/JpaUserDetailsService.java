package com.andres.curso.springboot.app.springbootcrud.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.andres.curso.springboot.app.springbootcrud.entities.User;
import com.andres.curso.springboot.app.springbootcrud.repositories.UserRepository;

// Marca esta clase como un servicio de Spring para que pueda ser inyectado donde se necesite
@Service
public class JpaUserDetailsService implements UserDetailsService {

    // Inyección automática del repositorio de usuarios (acceso a la base de datos)
    @Autowired
    private UserRepository repository;


    // Este método se ejecuta dentro de una transacción de solo lectura
    @Transactional(readOnly = true)
    @Override
    ///Se ejecuta en el login valida las credenciales
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Busca un usuario en la base de datos por su nombre de usuario
        Optional<User> userOptional = repository.findByUsername(username);

        // Si no se encuentra el usuario, lanza una excepción
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException(String.format("Username %s no existe en el sistema!", username));
        }

        // Obtiene el usuario del Optional (ya validado que no está vacío)
        User user = userOptional.orElseThrow();
        ///OrElseThrow lanza una exception si esta vacio

        // Convierte la lista de roles del usuario a una lista de autoridades (GrantedAuthority) para Spring Security
        List<GrantedAuthority> authorities = user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority(role.getName()))
            .collect(Collectors.toList());
            ///GrantedAuthority es una interfaz
            /// SimpleGrantedAuthority implementacion concreta


        // Retorna un objeto User de Spring Security, que contiene la información necesaria para la autenticación
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),     // nombre de usuario
            user.getPassword(),     // contraseña (encriptada)
            user.isEnabled(),       // si el usuario está habilitado
            true,                   // cuenta no expirada
            true,                   // credenciales no expiradas
            true,                   // cuenta no bloqueada
            authorities             // lista de autoridades (roles)
        );
    }
}

