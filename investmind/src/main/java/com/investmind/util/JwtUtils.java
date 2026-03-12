package com.investmind.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成、验证和解析JWT令牌
 */
@Slf4j
@Component
public class JwtUtils {

    @Value("${investmind.jwt.secret:investmindSecretKey2024}")
    private String jwtSecret;

    @Value("${investmind.jwt.access-token-expiration:86400}")
    private Long accessTokenExpiration; // 默认24小时

    @Value("${investmind.jwt.refresh-token-expiration:604800}")
    private Long refreshTokenExpiration; // 默认7天

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成访问令牌
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT令牌
     */
    public String generateAccessToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("tokenType", "ACCESS");

        return generateToken(claims, accessTokenExpiration);
    }

    /**
     * 生成刷新令牌
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT刷新令牌
     */
    public String generateRefreshToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("tokenType", "REFRESH");

        return generateToken(claims, refreshTokenExpiration);
    }

    /**
     * 生成令牌
     *
     * @param claims     自定义声明
     * @param expiration 过期时间（秒）
     * @return JWT令牌
     */
    private String generateToken(Map<String, Object> claims, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    /**
     * 从令牌中提取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return Long.valueOf(claims.get("userId").toString());
        }
        return null;
    }

    /**
     * 从令牌中提取用户名
     *
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("username", String.class);
        }
        return null;
    }

    /**
     * 获取令牌类型
     *
     * @param token JWT令牌
     * @return 令牌类型
     */
    public String getTokenType(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("tokenType", String.class);
        }
        return null;
    }

    /**
     * 获取令牌过期时间
     *
     * @param token JWT令牌
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.getExpiration();
        }
        return null;
    }

    /**
     * 验证令牌是否有效
     *
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT token is malformed: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("JWT signature validation failed: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT token is empty or null: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 解析JWT令牌
     *
     * @param token JWT令牌
     * @return 声明信息
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 获取访问令牌过期时间（秒）
     *
     * @return 过期时间
     */
    public Long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    /**
     * 获取刷新令牌过期时间（秒）
     *
     * @return 过期时间
     */
    public Long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}
