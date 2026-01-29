package org.example.springecommerceapi.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.expiration}")
    private int jwtExpirationMs;
    private SecretKey key;
    @PostConstruct
    public void init() {
        if (jwtSecret == null) {
            System.err.println("WARNING: jwt.secret is not set; generating a random signing key for runtime. Set jwt.secret in application.properties for persistent tokens.");
            this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            return;
        }

        byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            System.err.println("WARNING: Provided jwt.secret is too short (" + secretBytes.length*8 + " bits). Deriving a 256-bit key with SHA-256; consider using a stronger secret (at least 256 bits) to avoid this step.");
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hashed = digest.digest(secretBytes);
                this.key = Keys.hmacShaKeyFor(hashed);
            } catch (NoSuchAlgorithmException e) {
                // Should not happen; fallback to random key if SHA-256 is unavailable
                System.err.println("ERROR: SHA-256 unavailable, falling back to a random JWT key: " + e.getMessage());
                this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            }
        } else {
            this.key = Keys.hmacShaKeyFor(secretBytes);
        }
    }
    // Generate JWT token
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    // Get username from JWT token
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    // Validate JWT token
    public boolean validateJwtToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException e) {
            System.out.println("Invalid JWT signature: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.out.println("JWT token is expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println("JWT token is unsupported: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("JWT claims string is empty: " + e.getMessage());
        }
        return false;
    }


}