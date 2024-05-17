package pl.chrapatij.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import pl.chrapatij.backend.entity.User;
import pl.chrapatij.backend.exception.userExceptionError401;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {
    public static final String BEARER_PREFIX = "Bearer ";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expirationMs}")
    private long expirationMs;

    public String createToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof User customUserDetails) {
            claims.put("id", customUserDetails.getId());
            claims.put("login", customUserDetails.getLogin());
            claims.put("roles", customUserDetails.getRoles());
        }
        return createToken(claims, userDetails);
    }

    public boolean tokenIsValid(String token, UserDetails userDetails) {
        token = extractToken(token);
        return (getLogin(token).equals(userDetails.getUsername()) && !tokenExpired(token));
    }

    public String getLogin(String token) throws userExceptionError401 {
        token = extractToken(token);
        try {
            return getClaim(token, Claims::getSubject);
        } catch (ExpiredJwtException e) {
            sendError("Token expired.");
        } catch (UnsupportedJwtException e) {
            sendError("Token unsupported.");
        } catch (MalformedJwtException e) {
            sendError("Malformed token.");
        } catch (Exception e) {
            sendError("Invalid token.");
        }
        return null;
    }

    private String createToken(Map<String, Object> claims, UserDetails userDetails) {
        return Jwts
                .builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSecretKey())
                .compact();
    }

    private boolean tokenExpired(String token) {
        return getExpirationDate(token).before(new Date());
    }

    private Date getExpirationDate(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    private <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(getAllClaims(token));
    }

    private Claims getAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    private void sendError(String message) throws userExceptionError401 {
        throw new userExceptionError401("Unauthorized error: " + message);
    }

    private String extractToken(String token) {
        return StringUtils.startsWith(token, BEARER_PREFIX) ? token.substring(BEARER_PREFIX.length()) : token;
    }
}