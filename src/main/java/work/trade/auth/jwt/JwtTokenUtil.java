package work.trade.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import work.trade.auth.role.Role;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class JwtTokenUtil {

    public enum TokenType {
        ACCESS, REFRESH
    }

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh.expiration}")
    private long refreshTokenExpiration;

    private SecretKey signingKey;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


//****************************************//

    private final String refreshKeyPrefix = "refreshToken:";

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @PostConstruct
    private void initializeSigningKey() {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

//****************************************//

    //토큰 생성
    private String createToken(String userId, List<Role> roles, TokenType type, long expiration) {
        Claims claims = Jwts.claims().setSubject(userId);
        claims.put("type", type.name()); // Enum의 이름을 문자열로 저장
        if (roles != null) {
            claims.put("roles", roles.stream().map(Role::name).toList());
        }

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createAccessToken(String userId, List<Role> roles) {
        return createToken(userId, roles, TokenType.ACCESS, accessTokenExpiration);
    }

    //토큰에서 사용자 ID 추출
    public String getUserIdFromToken(String token) {
        return parseClaims(token)
                .getSubject();
    }

    //토큰에서 권한 추출
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Object roles = parseClaims(token).get("roles");
        return roles != null ? (List<String>) roles : List.of();
    }

    public String getTypeFromToken(String token) {
        return (String)parseClaims(token).get("type");
    }

    //토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.info("ExpiredJwtException : {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.info("SignatureException : {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.info("JwtException : {}", e.getMessage());
            return false;
        }
    }


//Refresh*************************//

    public String createRefreshToken(String userId) {
        return createToken(userId, null, TokenType.REFRESH, refreshTokenExpiration);
    }

    public void saveRefreshTokenToRedis(String refreshToken, String userId) {
        String key = refreshKeyPrefix + refreshToken;

        //Redis에 저장
        redisTemplate.opsForValue().set(
                key,
                userId,
                Duration.ofMillis(refreshTokenExpiration)
        );

        log.info("RefreshToken saved to Redis - userId: {}", userId);
    }

    public boolean isValidRefreshToken(String refreshToken) {
        // JWT 토큰 검증
        if (!validateToken(refreshToken)) {
            log.warn("RefreshToken JWT validation failed");
            return false;
        }

        //TokenType이 RefreshToken인지 확인
        if (!getTypeFromToken(refreshToken).equals(TokenType.REFRESH.name())) {
            log.warn("RefreshToken JWT is not REFRESH Token");
            return false;
        }

        // Redis에 존재하는지 확인
        String key = refreshKeyPrefix + refreshToken;
        Boolean exists = redisTemplate.hasKey(key);

        if (exists == null || !exists) {
            log.warn("RefreshToken not found in Redis");
            return false;
        }

        return true;
    }

    //Redis에서 RefreshToken 삭제
    public void invalidateRefreshToken(String refreshToken) {
        String key = refreshKeyPrefix + refreshToken;
        redisTemplate.delete(key);
        log.info("RefreshToken invalidated in Redis");
    }

    public void rotateRefreshToken(String oldRefreshToken,
                                   String newRefreshToken,
                                   String userId) {
        // 이전 토큰 무효화
        invalidateRefreshToken(oldRefreshToken);

        // 새 토큰 저장
        saveRefreshTokenToRedis(newRefreshToken, userId);

        log.info("RefreshToken rotated - userId: {}", userId);
    }

    public String getUserIdFromRefreshToken(String refreshToken) {
        return parseClaims(refreshToken).getSubject();
    }

}
