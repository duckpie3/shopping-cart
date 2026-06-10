package com.invoice.config.jwt;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

@Component
public class JwtUtil {

    private final SecretKey secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.secretKey = new SecretKeySpec(Base64.getDecoder().decode(secret), "HmacSHA256");
    }

    public Claims extractClaims(String token) {
    	
    	JwtParser jwtParser = Jwts.parserBuilder()
    			.setSigningKey(secretKey)
    			.build();
    	
    	return jwtParser.parseClaimsJws(token).getBody();
    	
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

	@SuppressWarnings("unchecked")
	public List<HashMap<String, String>> extractPermisos(String token) {
         return extractClaims(token).get("roles", List.class);
    }

    public boolean isTokenValid(String token, String username) {
        return extractUsername(token).equals(username) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractClaims(token));
    }
    
    public Integer extractUserId(String token) {
        Object id = extractClaims(token).get("id");
        if (id == null) {
            throw new IllegalArgumentException("Token sin claim 'id'");
        }
        return Integer.parseInt(id.toString());
   }
}
