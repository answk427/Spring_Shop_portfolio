package work.trade.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
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
@RequiredArgsConstructor
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

    private final RedisTemplate<String, String> redisTemplate;


//****************************************//

    private final String refreshKeyPrefix = "refreshToken:";
    private static final String BLACKLIST_KEY_PREFIX = "blacklist:";


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
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or blank");
        }


        Claims claims = Jwts.claims().setSubject(userId);
        claims.put("type", type.name()); // Enum의 이름을 문자열로 저장
        if (roles != null && !roles.isEmpty()) {
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
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("Roles cannot be null or empty");
        }
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
            log.warn("Token expired: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.warn("JWT error: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
            return false;
        }
    }

    public boolean isValidAccessToken(String accessToken) {
        //JWT 검증
        if (!validateToken(accessToken)) {
            return false;
        }

        //토큰 타입 검증
        if (!getTypeFromToken(accessToken).equals(TokenType.ACCESS.name())) {
            log.warn("Token is not ACCESS Token");
            return false;
        }

        //블랙리스트 확인
        String userId = getUserIdFromToken(accessToken);
        if (isAccessTokenBlacklisted(accessToken, userId)) {
            log.warn("AccessToken is blacklisted");
            return false;
        }

        return true;
    }


//Refresh*************************//

    public String createRefreshToken(String userId) {
        return createToken(userId, null, TokenType.REFRESH, refreshTokenExpiration);
    }

    public void saveRefreshTokenToRedis(String refreshToken, String userId) {
        String key = refreshKeyPrefix + refreshToken;

        //Redis에 저장
        try {
            redisTemplate.opsForValue().set(
                    key,
                    userId,
                    Duration.ofMillis(refreshTokenExpiration));
        } catch (Exception e) {
            log.error("Failed to save RefreshToken to Redis", e);
            throw new RuntimeException("Redis operation failed", e);
        }

        log.info("RefreshToken saved to Redis - userId: {}", userId);
    }

    public void blacklistAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return;
        }

        String key = BLACKLIST_KEY_PREFIX + accessToken;
        try {
            long remainingTime = extractExpirationTime(accessToken) - System.currentTimeMillis();

            if (remainingTime > 0) {
                redisTemplate.opsForValue().set(
                        key,
                        "blacklisted",
                        Duration.ofMillis(remainingTime)
                );
                log.debug("AccessToken blacklisted");
            }
        } catch (DataAccessException e) {
            log.error("Failed to blacklist AccessToken", e);
            throw e;
        }
    }

    //AccessToken 블랙리스트 확인
    public boolean isAccessTokenBlacklisted(String accessToken, String userId) {
        try {
            String key = BLACKLIST_KEY_PREFIX + accessToken;
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.warn("Failed to check blacklist - userId: {} - {}", userId, e.getMessage());
            return false;
        }
    }

    //토큰 만료시간 추출
    private long extractExpirationTime(String token) {
        try {
            return parseClaims(token).getExpiration().getTime();
        } catch (Exception e) {
            log.warn("Failed to extract expiration time");
            return System.currentTimeMillis();
        }
    }

    public boolean isValidRefreshToken(String refreshToken) {
        // JWT 토큰 검증
        if (!validateToken(refreshToken)) {
            log.warn("RefreshToken JWT validation failed");
            return false;
        }

        //TokenType 검증
        if (!getTypeFromToken(refreshToken).equals(TokenType.REFRESH.name())) {
            log.warn("Token is not REFRESH Token");
            return false;
        }

        // Redis에 존재하는지 확인
        String key = refreshKeyPrefix + refreshToken;
        Boolean exists;
        try {
            exists = redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.warn("RedisTemplate.hasKey() failed : {}", e.getMessage());
            return false;
        }

        if (!exists) {
            log.warn("RefreshToken not found in Redis");
            return false;
        }

        return true;
    }

    //Redis에서 RefreshToken 삭제
    public void invalidateRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        String key = refreshKeyPrefix + refreshToken;
        try {
            redisTemplate.delete(key);
        } catch (DataAccessException e) {
            log.error("Failed Delete RefreshToken from Redis", e);
            throw e;
        }

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
