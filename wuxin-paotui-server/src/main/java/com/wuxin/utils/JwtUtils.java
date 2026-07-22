package com.wuxin.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public final class JwtUtils {

    private static final String DEFAULT_SECRET =
            "wuxin-paotui-jwt-secret-key-for-development-2026";

    private static final long EXPIRE_TIME = 7L * 24 * 60 * 60 * 1000;

    private static volatile SecretKey secretKey = buildSecretKey(DEFAULT_SECRET);

    private JwtUtils() {
    }

    public static void configureSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            secretKey = buildSecretKey(DEFAULT_SECRET);
            return;
        }
        secretKey = buildSecretKey(secret);
    }

    public static String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRE_TIME);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public static Long getUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    public static String getUsername(String token) {
        return parseClaims(token).get("username", String.class);
    }

    public static boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private static Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static SecretKey buildSecretKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
