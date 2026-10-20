package tz.ac.dit.ipttracker.security;

import tz.ac.dit.ipttracker.entity.Role;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** The authenticated principal rebuilt from the JWT claims on every request (stateless). */
public record AppUserDetails(Long id, String email, Role role) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));   // ROLE_ prefix => hasRole() works
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
