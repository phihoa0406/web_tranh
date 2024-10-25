package com.example.web_tranh.service.Jwt;

import com.example.web_tranh.entity.Role;
import com.example.web_tranh.entity.User;
import com.example.web_tranh.service.UserSecurity.UserSecurityService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Jwts;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtService {
    private static final String KEY_SECRET = "MTIzNDU2NDU5OThEMzIxM0F6eGMzNTE2NTQzMjEzMjE2NTQ5OHEzMTNhMnMxZDMyMnp4M2MyMQ==";
    @Autowired
    private UserSecurityService userSecurityService;

    // Tạo jwt dựa trên username (tạo thông tin cần trả về cho FE khi đăng nhập thành công)
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        User user = userSecurityService.findByUsername(username);
        claims.put("id", user.getIdUser());
        claims.put("avatar", user.getAvatar());
        claims.put("lastName", user.getLastName());
        claims.put("enabled", user.isEnabled());
        List<Role> roles = user.getListRoles();
        if (roles.size() > 0) {
            for (Role role : roles) {
                if (role.getNameRole().equals("ADMIN")) {
                    claims.put("role", "ADMIN");
                    break;
                }
                if (role.getNameRole().equals("ARTIST")) {
                    claims.put("role", "ARTIST");
                    break;
                }
                if (role.getNameRole().equals("CUSTOMER")) {
                    claims.put("role", "CUSTOMER");
                    break;
                }
            }
        }


        return createToken(claims, username);
    }

    private static final long EXPIRATION_DURATION = 30 * 60 * 1000; // 30 phút (ms)

    private String createToken(Map<String, Object> claims, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_DURATION);

        return Jwts.builder()
                .setClaims(claims)                      // dữ liệu đính kèm (id, role, avatar...)
                .setSubject(username)                  // username chính là người dùng
                .setIssuedAt(now)                      // thời gian phát hành
                .setExpiration(expiryDate)             // thời gian hết hạn
                .signWith(getSigneKey(), SignatureAlgorithm.HS256) // ký bằng khóa bí mật
                .compact();
    }

    // Lấy key_secret
    private Key getSigneKey() {
        byte[] keyByte = Decoders.BASE64.decode(KEY_SECRET);
        return Keys.hmacShaKeyFor(keyByte);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigneKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    // Trích xuất thông tin cụ thể nhưng triển khai tổng quát (Method Generic)
    public <T> T extractClaims(String token, Function<Claims, T> claimsTFunction) {
        final Claims claims = extractAllClaims(token);
        return claimsTFunction.apply(claims);
    }

    // Lấy ra thời gian hết hạn
    public Date extractExpiration(String token) {
        return extractClaims(token, Claims::getExpiration);
    }

    // Lấy ra username
    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    // Kiểm tra token đó hết hạn chưa
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Kiểm tra tính hợp lệ của token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
