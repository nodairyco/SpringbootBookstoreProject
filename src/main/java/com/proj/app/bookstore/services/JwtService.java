package com.proj.app.bookstore.services;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;
import java.util.function.Function;

public interface JwtService {
    String extractUsername(String jwtToken);
    <T> T extractClaim(String token, Function<Claims, T> function);
    String generateToken(Map<String, Object> extraClaims, UserDetails details);
    String generateToken(UserDetails details);
    boolean tokenValid(String token, UserDetails userDetails);
}
