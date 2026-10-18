package com.intellibank.security;

import com.intellibank.domain.UserAccount;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey signingKey;
    private final long expirationSeconds;

    public JwtService(@Value("${intellibank.security.jwt-secret}") String secret,
                      @Value("${intellibank.security.jwt-expiration-seconds:3600}") long expirationSeconds) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) throw new IllegalArgumentException("JWT_SECRET must be at least 32 UTF-8 bytes");
        if (expirationSeconds < 60 || expirationSeconds > 86400) {
            throw new IllegalArgumentException("JWT_EXPIRATION_SECONDS must be between 60 and 86400");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationSeconds = expirationSeconds;
    }

    public String createToken(UserAccount account) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(account.getEmail())
                .claim("role", account.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(signingKey)
                .compact();
    }

    public String subject(String token) {
        return Jwts.parser().verifyWith(signingKey).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public long expirationSeconds() { return expirationSeconds; }
}
