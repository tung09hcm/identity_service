package com.example.identity_service.configuration;

import com.example.identity_service.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;

@Configuration // Đánh dấu đây là một class cấu hình cho Spring (được scan và thực thi khi ứng dụng khởi động)
@EnableWebSecurity // Bật bảo mật web Spring Security (tương đương với cấu hình WebSecurityConfigurerAdapter trước đây)
@EnableMethodSecurity // Cho phép dùng các annotation phân quyền như @PreAuthorize, @Secured trên controller hoặc service
public class SecurityConfig {

    // Danh sách các endpoint công khai (không yêu cầu xác thực)
    private final String[] PUBLIC_ENDPOINTS = {
            "/users",
            "/auth/token",
            "/auth/introspect",
            "/auth/logout",
            "/auth/refresh"
    };

    @Autowired
    private CustomJwtDecoder customJwtDecoder;

    // Đọc giá trị khóa ký JWT từ file application.properties (hoặc biến môi trường)
    @Value("${jwt.signerKey}")
    private String signerKey;

    /**
     * Cấu hình chính cho bảo mật HTTP
     * Trả về một SecurityFilterChain (thay thế cách cũ là override configure())
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        // Cấu hình phân quyền cho request
        httpSecurity.authorizeHttpRequests(request ->
                request
                        .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll() // Cho phép gọi POST vào các endpoint công khai
                        .anyRequest().authenticated() // Mọi request khác đều phải xác thực
        );

        // Cấu hình xác thực bằng JWT (OAuth2 Resource Server)
        httpSecurity.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwtConfigurer ->
                                jwtConfigurer
                                        .decoder(customJwtDecoder) // Cung cấp decoder để giải mã JWT
                                        .jwtAuthenticationConverter(jwtAuthenticationConverter()) // Mapping từ JWT → GrantedAuthorities
                        )
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint()) // Handler khi token không hợp lệ
        );

        // Vô hiệu hóa CSRF (chống giả mạo request) vì API REST thường không cần
        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        return httpSecurity.build();
    }

    /**
     * Trả về một JwtAuthenticationConverter
     * Mục tiêu là chuyển đổi nội dung JWT thành Authentication object có quyền
     * Spring Security sẽ dùng object này để phân quyền các endpoint
     *
     * Cụ thể: nó lấy claim "authorities" trong JWT (hoặc "scope") và gán prefix ROLE_
     */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

        // Prefix ROLE_ để đúng format của Spring Security (ví dụ: ROLE_ADMIN)
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        // Gắn converter vào JwtAuthenticationConverter
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }

    /**
     * Trả về PasswordEncoder dùng để mã hóa mật khẩu
     * Ở đây sử dụng BCrypt với strength 10 (số vòng lặp hashing)
     *
     * BCrypt tự động sinh salt nội bộ cho từng lần encode → an toàn chống rainbow table
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
