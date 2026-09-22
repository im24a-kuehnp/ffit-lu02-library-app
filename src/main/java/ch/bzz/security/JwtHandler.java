package ch.bzz.security;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Properties;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtHandler {
    private static final SecretKey JWT_KEY = Keys.hmacShaKeyFor(loadSecret().getBytes(StandardCharsets.UTF_8));

    private JwtHandler() {
    }

    public static String createJwt(String subject, Integer userId) {
        Date currentTime = new Date();
        Date expirationTime = new Date(currentTime.getTime() + 3_600_000);

        return Jwts.builder()
                .subject(subject)
                .claim("userId", userId)
                .issuedAt(currentTime)
                .expiration(expirationTime)
                .signWith(JWT_KEY)
                .compact();
    }

    private static String loadSecret() {
        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream("config.properties")) {
            properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not read config.properties. Copy config.properties.template to config.properties and fill in JWT_SECRET.",
                    e);
        }
        String secret = properties.getProperty("JWT_SECRET");
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET in config.properties must be set to at least 32 characters");
        }
        return secret;
    }
}
