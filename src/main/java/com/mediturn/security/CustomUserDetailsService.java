package com.mediturn.security;

import com.mediturn.domain.User;
import com.mediturn.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Spring Security llama a este método con el "username".
     * En nuestro caso puede ser email (login) o UUID string (filtro JWT).
     */
    @Override
    public UserDetails loadUserByUsername(String emailOrId) throws UsernameNotFoundException {
        User user;
        try {
            // Si es un UUID válido, buscamos por ID (flujo JWT)
            java.util.UUID id = java.util.UUID.fromString(emailOrId);
            user = userRepository.findById(id)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + emailOrId));
        } catch (IllegalArgumentException ex) {
            // Si no es UUID, buscamos por email (flujo login)
            user = userRepository.findByEmail(emailOrId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + emailOrId));
        }

        return new CustomUserDetails(user, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
