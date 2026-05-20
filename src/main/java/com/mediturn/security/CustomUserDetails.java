package com.mediturn.security;

import com.mediturn.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Adapta nuestra entidad User al contrato que espera Spring Security.
 * Los roles los cargamos por separado según la organización activa.
 */
public class CustomUserDetails implements UserDetails {

    @Getter
    private final UUID userId;
    private final String email;
    private final String passwordHash;
    private final boolean active;
    private final List<GrantedAuthority> authorities;

    public CustomUserDetails(User user, List<GrantedAuthority> authorities) {
        this.userId       = user.getId();
        this.email        = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.active       = user.isActive();
        this.authorities  = authorities;
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword()   { return passwordHash; }
    @Override public String getUsername()   { return email; }
    @Override public boolean isEnabled()    { return active; }
    @Override public boolean isAccountNonExpired()    { return true; }
    @Override public boolean isAccountNonLocked()     { return true; }
    @Override public boolean isCredentialsNonExpired(){ return true; }
}
