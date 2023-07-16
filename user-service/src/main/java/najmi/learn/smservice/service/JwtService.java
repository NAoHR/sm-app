package najmi.learn.smservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {
    @Value("${spring.security.jwt.secret-key}")
    private String SECRET_KEY;

    @Value("${spring.security.jwt.expiration}")
    private Long EXPIRATION_ACCESS;

    @Value("${spring.security.jwt.refresh-token.expiration}")
    private Long EXPIRATION_REFRESH;

    public String extraxtUsername(String jwt){
        return extractClaim(jwt, Claims::getSubject);
    }

    public Date extractExpiration(String jwt){
        return extractClaim(jwt, Claims::getExpiration);
    }

    public String generateToken(UserDetails userDetails){
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return buildToken(extraClaims, userDetails, EXPIRATION_ACCESS);
    }

    public String generateRefreshToken(
            UserDetails userDetails
    ) {
        return buildToken(new HashMap<>(), userDetails, EXPIRATION_REFRESH);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ){
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigninKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails){
        final String username = extraxtUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String jwt){
        return extractExpiration(jwt).before(new Date());
    }

    private <T> T extractClaim(String jwt, Function<Claims, T> resolver){
        final Claims claims = extractAllClaims(jwt);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String jwt){
        try{
            return Jwts
                    .parserBuilder()
                    .setSigningKey(getSigninKey())
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();
        }catch (Exception e){
            log.info("token error: {}", e);
            return null;
        }
    }

    private Key getSigninKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }


}
