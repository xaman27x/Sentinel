package com.moderation.sentinel.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {
    
    @Value("${application.jwt.secret}")
    private String jwtSecret;
    
    @Value("${application.jwt.expiration:86400000}") // 24 Hours -> ms
    private long jwtExpirationMs;
    
    private SecretKey getSigningKey() {
        try {
            byte[] keyBytes;
            
            if (jwtSecret.length() < 64) {
                keyBytes = generateSecureKey();
                System.out.println("Generated new secure JWT key due to short provided key");
            } else {
                keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
                if (keyBytes.length > 64) {
                    byte[] truncated = new byte[64];
                    System.arraycopy(keyBytes, 0, truncated, 0, 64);
                    keyBytes = truncated;
                }
            }
            
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create JWT signing key: " + e.getMessage(), e);
        }
    }
    
    private byte[] generateSecureKey() {
        SecureRandom random = new SecureRandom();
        byte[] key = new byte[64]; // 512 bits
        random.nextBytes(key);
        return key;
    }
    
    public String generateToken(String email, Long userId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        
        try {
            return Jwts.builder()
                    .setSubject(userId.toString())
                    .claim("email", email)
                    .claim("role", role)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Error generating JWT token: " + e.getMessage(), e);
        }
    }
    
    public String getEmailFromToken(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.get("email", String.class);
        } catch (Exception e) {
            throw new RuntimeException("Error extracting email from token: " + e.getMessage(), e);
        }
    }
    
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = getClaims(token);
            String subject = claims.getSubject();
            return Long.parseLong(subject);
        } catch (Exception e) {
            throw new RuntimeException("Error extracting user ID from token: " + e.getMessage(), e);
        }
    }
    
    public String getRoleFromToken(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.get("role", String.class);
        } catch (Exception e) {
            throw new RuntimeException("Error extracting role from token: " + e.getMessage(), e);
        }
    }
    
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        try {
            getClaims(token);
            return true;
        } catch (SecurityException ex) {
            System.err.println("Invalid JWT signature: " + ex.getMessage());
        } catch (MalformedJwtException ex) {
            System.err.println("Invalid JWT token: " + ex.getMessage());
        } catch (ExpiredJwtException ex) {
            System.err.println("Expired JWT token: " + ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            System.err.println("Unsupported JWT token: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            System.err.println("JWT claims string is empty: " + ex.getMessage());
        } catch (Exception ex) {
            System.err.println("JWT validation error: " + ex.getMessage());
        }
        return false;
    }
    
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}