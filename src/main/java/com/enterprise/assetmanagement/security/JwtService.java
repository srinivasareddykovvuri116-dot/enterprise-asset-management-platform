package com.enterprise.assetmanagement.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expiration;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration}") long expiration) {

        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        this.expiration = expiration;
    }

    // ============================================================
    // GENERATE TOKEN
    // ============================================================

    public String generateToken(CustomUserDetails userDetails) {

        Date now = new Date();
        Date expiryDate = new Date(
                now.getTime() + expiration
        );

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("userId", userDetails.getUserId())
                .claim("organizationId", userDetails.getOrganizationId())
                .claim("role", userDetails.getRole())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    // ============================================================
    // EXTRACT USERNAME
    // ============================================================

    public String extractUsername(String token) {

        return extractAllClaims(token)
                .getSubject();
    }

    // ============================================================
    // EXTRACT USER ID
    // ============================================================

    public Long extractUserId(String token) {

        Number userId = extractAllClaims(token)
                .get("userId", Number.class);

        return userId.longValue();
    }

    // ============================================================
    // EXTRACT ORGANIZATION ID
    // ============================================================

    public Long extractOrganizationId(String token) {

        Number organizationId =
                extractAllClaims(token)
                        .get("organizationId", Number.class);

        return organizationId.longValue();
    }

    // ============================================================
    // EXTRACT ROLE
    // ============================================================

    public String extractRole(String token) {

        return extractAllClaims(token)
                .get("role", String.class);
    }

    // ============================================================
    // VALIDATE TOKEN
    // ============================================================

    public boolean isTokenValid(
            String token,
            CustomUserDetails userDetails) {

        String username = extractUsername(token);

        return username != null
                && username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    // ============================================================
    // CHECK TOKEN EXPIRATION
    // ============================================================

    private boolean isTokenExpired(String token) {

        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }

    // ============================================================
    // EXTRACT CLAIMS
    // ============================================================

    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}