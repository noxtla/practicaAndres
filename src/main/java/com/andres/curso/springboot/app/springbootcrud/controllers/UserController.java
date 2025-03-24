package com.andres.curso.springboot.app.springbootcrud.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.andres.curso.springboot.app.springbootcrud.entities.User;
import com.andres.curso.springboot.app.springbootcrud.services.UserService;

import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:4200", originPatterns = "*")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService service;

    @GetMapping
    public List<User> list() {
        return service.findAll();
    }

    @PreAuthorize("hasRole('ADMIN')") // Solo los usuarios con rol ADMIN pueden acceder a este método
    @PostMapping // Maneja solicitudes HTTP POST para crear un recurso
    public ResponseEntity<?> create(@Valid @RequestBody User user, BindingResult result) {

        // Si hay errores en la validación de los campos del usuario
        if (result.hasFieldErrors()) {
            // Retorna una respuesta con los errores de validación
            return validation(result);
        }

        // Si no hay errores, guarda el usuario y retorna una respuesta HTTP 201
        // (CREATED)
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.save(user));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user, BindingResult result) {
        user.setAdmin(false);
        return create(user, result);
    }

    // Método que construye una respuesta con los errores de validación de los
    // campos
    private ResponseEntity<?> validation(BindingResult result) {

        // Creamos un mapa para almacenar los errores: clave = nombre del campo, valor =
        // mensaje de error
        Map<String, String> errors = new HashMap<>();

        // Recorremos todos los errores de campo que contiene el BindingResult
        result.getFieldErrors().forEach(err -> {
            // Por cada error, agregamos una entrada al mapa: el nombre del campo y su
            // mensaje personalizado
            errors.put(
                    err.getField(), // nombre del campo con error (por ejemplo, "email")
                    "El campo " + err.getField() + " " + err.getDefaultMessage() // mensaje: "El campo email no debe
                                                                                 // estar vacío"
            );
        });

        // Devolvemos una respuesta HTTP 400 (Bad Request) con el mapa de errores en el
        // cuerpo
        return ResponseEntity.badRequest().body(errors);
    }
}
