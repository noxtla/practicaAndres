package com.andres.curso.springboot.app.springbootcrud.entities;

import java.util.ArrayList;
import java.util.List;

import com.andres.curso.springboot.app.springbootcrud.validation.ExistsByUsername;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*; // Importa todas las anotaciones de JPA
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Indicamos que esta clase es una entidad JPA, se mapeará a una tabla en la base de datos
@Entity
// Indicamos el nombre de la tabla que se generará en la base de datos
@Table(name = "users")
public class User {

    // Clave primaria de la tabla
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincremental
    private Long id;

    // Validación personalizada que verifica si el username ya existe
    @ExistsByUsername
    // No puede estar vacío
    @NotBlank
    // Longitud mínima de 4 y máxima de 12 caracteres
    @Size(min = 4, max = 12)
    // El username debe ser único en la base de datos
    @Column(unique = true)
    private String username;

    // No puede estar vacío
    @NotBlank
    // Solo se puede escribir el password (no se mostrará al hacer GET)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    // Evitamos que se serialicen propiedades problemáticas de Hibernate en JSON
    @JsonIgnoreProperties({"users", "handler", "hibernateLazyInitializer"})
    // Relación muchos a muchos con la entidad Role
    @ManyToMany
    @JoinTable(
        name = "users_roles", // Nombre de la tabla intermedia
        joinColumns = @JoinColumn(name = "user_id"), // Clave foránea al usuario
        inverseJoinColumns = @JoinColumn(name = "role_id"), // Clave foránea al rol
        uniqueConstraints = { @UniqueConstraint(columnNames = {"user_id", "role_id"}) } // Evita duplicados
    )
    private List<Role> roles; // Lista de roles del usuario

    // Constructor que inicializa la lista de roles
    public User() {
        roles = new ArrayList<>();
    }

    // Indica si el usuario está habilitado (activo)
    private boolean enabled;

    // Campo transitorio (no se guarda en la base de datos), solo se puede escribir desde el JSON
    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private boolean admin;

    // Método que se ejecuta antes de guardar un nuevo usuario
    // Aquí lo usamos para que el usuario se guarde habilitado por defecto
    @PrePersist
    public void prePersist() {
        enabled = true;
    }

    // Métodos getter y setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    // hashCode y equals para comparar usuarios, basados en id y username
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        return true;
    }

}
