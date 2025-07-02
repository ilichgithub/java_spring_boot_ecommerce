package com.ilich.sb.e_commerce.util;

import com.ilich.sb.e_commerce.model.User;
import com.ilich.sb.e_commerce.repository.IUserRepository;
import com.ilich.sb.e_commerce.service.impl.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserUtil {

    private final IUserRepository userRepository;

    @Autowired
    public UserUtil(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }
    // Helper para obtener el usuario autenticado
    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Asegúrate de que el usuario esté autenticado y sea de tipo UserDetailsImpl
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            throw new RuntimeException("User not authenticated."); // Esto no debería ocurrir con @PreAuthorize
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database."));
    }
}
