package tz.ac.dit.ipttracker.service;

import tz.ac.dit.ipttracker.dto.LoginRequest;
import tz.ac.dit.ipttracker.dto.TokenResponse;
import tz.ac.dit.ipttracker.dto.UserResponse;
import tz.ac.dit.ipttracker.security.CurrentUser;
import tz.ac.dit.ipttracker.security.JwtService;
import tz.ac.dit.ipttracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest r) {
        var user = users.findByEmail(r.email().trim().toLowerCase())
                .filter(u -> u.isActive() && passwordEncoder.matches(r.password(), u.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        return new TokenResponse(jwtService.generateToken(user), "Bearer", jwtService.expiresInSeconds());
    }

    public UserResponse me() {
        return userService.get(CurrentUser.get().id());
    }
}
