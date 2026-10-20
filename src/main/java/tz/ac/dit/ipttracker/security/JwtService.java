package tz.ac.dit.ipttracker.security;

import tz.ac.dit.ipttracker.entity.Role;
import tz.ac.dit.ipttracker.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey key;
    private final Duration lifetime;

    public JwtService(@Value("${app.jwt.secret}") String base64Secret,
                      @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        byte[] bytes = Decoders.BASE64.decode(base64Secret);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret must be Base64 of at least 32 bytes (256 bits)");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.lifetime = Duration.ofMinutes(expirationMinutes);
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("uid", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(lifetime)))
                .signWith(key)
                .compact();
    }

    public long expiresInSeconds() {
        return lifetime.toSeconds();
    }

    /** Returns the principal if the token is valid and unexpired, otherwise empty. */
    public Optional<AppUserDetails> parse(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return Optional.of(new AppUserDetails(c.get("uid", Long.class), c.getSubject(),
                    Role.valueOf(c.get("role", String.class))));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
