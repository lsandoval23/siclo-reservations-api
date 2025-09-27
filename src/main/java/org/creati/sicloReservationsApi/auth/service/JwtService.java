package org.creati.sicloReservationsApi.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.creati.sicloReservationsApi.auth.dto.UserJwtInfo;
import org.creati.sicloReservationsApi.auth.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private final String secretKey;
    private final Long jwtExpiration;
    private final ObjectMapper objectMapper;

    public JwtService(
            @Value("${jwt.secret:mySecretKey12345678901234567890}") final String secretKey,
            @Value("${jwt.expiration:86400000}") final Long jwtExpiration,
            final ObjectMapper objectMapper) {
        this.secretKey = secretKey;
        this.jwtExpiration = jwtExpiration;
        this.objectMapper = objectMapper;
    }


    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        // Get Signing key
        byte[] keyBytes = secretKey.getBytes();
        SecretKey signKey = Keys.hmacShaKeyFor(keyBytes);

        final Claims claims = Jwts.parserBuilder()
                .setSigningKey(signKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }

    public String generateToken(User user) {
        try {
            // Build dtos
            Map<String, Object> extraClaims = new HashMap<>();
            UserJwtInfo userInfo = new UserJwtInfo(user);
            String userInfoJson = objectMapper.writeValueAsString(userInfo);

            // Set claims
            extraClaims.put("userInfo", userInfoJson);
            extraClaims.put("userId", user.getId());

            // Get Signing key
            byte[] keyBytes = secretKey.getBytes();
            SecretKey signKey = Keys.hmacShaKeyFor(keyBytes);

            // Build token
            return Jwts.builder()
                    .setClaims(extraClaims)
                    .setSubject(user.getUsername())
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                    .signWith(signKey, SignatureAlgorithm.HS256)
                    .compact();

        } catch (Exception e) {
            throw new RuntimeException("Error generating JWT token", e);
        }
    }


    public boolean isTokenValid(String token, UserDetails userDetails) {

        final String username = extractClaim(token, Claims::getSubject);
        final Date expiration = extractClaim(token, Claims::getExpiration);
        final boolean isTokenExpired = expiration.before(new Date());

        return (username.equals(userDetails.getUsername())) && !isTokenExpired;
    }


    public SecretKey getSignInKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public UserJwtInfo extractUserInfo(String token) {
        try {
            String userInfoJson = extractClaim(token, claims -> (String) claims.get("userInfo"));
            return objectMapper.readValue(userInfoJson, UserJwtInfo.class);
        } catch (Exception e) {
            throw new RuntimeException("Error extracting user info from JWT", e);
        }
    }



}
