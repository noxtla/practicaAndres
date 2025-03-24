package com.andres.curso.springboot.app.springbootcrud.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.andres.curso.springboot.app.springbootcrud.entities.Role;
import com.andres.curso.springboot.app.springbootcrud.entities.User;
import com.andres.curso.springboot.app.springbootcrud.repositories.RoleRepository;
import com.andres.curso.springboot.app.springbootcrud.repositories.UserRepository;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository repository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return (List<User>) repository.findAll();
    }

    @Override
    @Transactional
    public User save(User user) {


        ///Ve y busca en la DB los roles por nombre
        Optional<Role> optionalRoleUser = roleRepository.findByName("ROLE_USER");
        
        ///Crea la lista roles
        List<Role> roles = new ArrayList<>();

        ///Si es role user lo agregas
        optionalRoleUser.ifPresent(roles::add);

        ///isAdmin es un boolean esa bandera viene en el JSON
        if (user.isAdmin()) {
            ///Buscas los roles por admin
            Optional<Role> optionalRoleAdmin = roleRepository.findByName("ROLE_ADMIN");
            ///Si esta presente admin lo agregas a la lista de roles
            optionalRoleAdmin.ifPresent(roles::add);
            ///Estas dos lienas son iguales
            ///optionalRoleAdmin.ifPresent(role -> roles.add(role));
        }

        ///Agregas los roles al usuario
        user.setRoles(roles);
        ///Agregas el password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        ///Guardas el usuario con su rol o roles
        return repository.save(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return repository.existsByUsername(username);
    }
    
}
