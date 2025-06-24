package com.ilich.sb.e_commerce.service.impl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ilich.sb.e_commerce.model.User;
import com.ilich.sb.e_commerce.repository.IUserRepository;

@Service // Marca como un servicio de Spring
public class UserDetailsServiceImpl implements UserDetailsService {

    private final IUserRepository userRepository;

    public UserDetailsServiceImpl(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Este método es llamado por Spring Security para cargar los detalles de un usuario
     * dado su nombre de usuario (username, que en nuestro caso será único como un email).
     *
     * @param username El nombre de usuario (ej. email) del usuario que se intenta autenticar.
     * @return Un objeto UserDetails que contiene la información del usuario y sus roles/permisos.
     * @throws UsernameNotFoundException Si el usuario con el nombre de usuario dado no existe.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Intentamos buscar el usuario en la base de datos usando nuestro UserRepository.
        // Usamos Optional para manejar el caso de que el usuario no sea encontrado de forma segura.
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con username: " + username));

        // 2. Si el usuario es encontrado, lo devolvemos.
        // Como nuestra entidad 'User' implementa 'UserDetails', podemos devolverla directamente.
        // Spring Security usará los métodos getUsername(), getPassword() y getAuthorities()
        // de tu entidad User para realizar la autenticación y autorización.
        return UserDetailsImpl.build(user);
    }
}