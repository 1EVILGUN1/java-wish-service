package pet.project.wish.service;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access.expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpiration;

    // Генерация access токена
    public String generateAccessToken(Long userId) {
        return generateToken(userId, accessExpiration);
    }

    // Генерация refresh токена
    public String generateRefreshToken(Long userId) {
        return generateToken(userId, refreshExpiration);
    }

    // Общий метод генерации токена
    private String generateToken(Long userId, long expiration) {
        return Jwts.builder()
                .setSubject(userId.toString()) // Устанавливаем ID пользователя как subject
                .setIssuedAt(new Date()) // Время выпуска
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // Время истечения
                .signWith(SignatureAlgorithm.HS512, secret) // Подпись с использованием HS512 и секретного ключа
                .compact();
    }

    // Получение данных (claims) из токена
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    // Валидация токена
    public void validateToken(String token) throws JwtException {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            log.error("Token has expired: {}", token, e);
            throw new JwtException("Token has expired");
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            log.error("Invalid token: {}", token, e);
            throw new JwtException("Invalid token");
        }
    }

    // Извлечение ID пользователя из токена
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.parseLong(claims.getSubject());
    }
}