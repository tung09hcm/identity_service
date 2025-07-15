package com.example.identity_service.service;

import com.example.identity_service.dto.request.AuthenticationRequest;
import com.example.identity_service.dto.request.IntrospecRequest;
import com.example.identity_service.dto.response.AuthenticationResponse;
import com.example.identity_service.dto.response.IntrospecResponse;
import com.example.identity_service.entity.User;
import com.example.identity_service.exception.AppException;
import com.example.identity_service.exception.ErrorCode;
import com.example.identity_service.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;
    
    public AuthenticationResponse authenticate(AuthenticationRequest request)
    {
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if(!authenticated)
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        var token = generateToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .isAuthenticated(true)
                .build();
    }

    public IntrospecResponse introspect(IntrospecRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        return IntrospecResponse.builder()
                .valid(verified && expiryTime.after(new Date()))
                .build();
    }

    /**
     * Tạo JWT (JSON Web Token) cho người dùng đã xác thực
     * Token được ký bằng thuật toán HMAC SHA-512 (HS512)
     *
     * ➤ Thư viện dùng: com.nimbusds:nimbus-jose-jwt
     * ➤ Token trả ra là chuỗi JWT đã được ký (compact serialized string)
     *
     * @param user - thực thể người dùng đã đăng nhập
     * @return chuỗi JWT hợp lệ, có chữ ký, mang thông tin xác thực của user
     */
    private String generateToken(User user) {

        // Tạo phần header của JWT, chỉ định thuật toán ký là HS512
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        // Xây dựng phần payload (claims) của JWT
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())          // định danh người dùng (thường là username)
                .issuer("dunno.com")                  // tên hệ thống phát hành token
                .issueTime(new Date())                // thời gian token được tạo
                .expirationTime(new Date(             // thời gian token hết hạn (sau 1 giờ)
                        Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()
                ))
                .claim("scope", buildScope(user))     // custom claim chứa danh sách quyền (roles)
                .build();

        // Chuyển claims thành payload JSON
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        // Kết hợp header và payload để tạo đối tượng JWSObject (JWT chưa ký)
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        try {
            // Ký token bằng khóa bí mật (HMAC-SHA512)
            // MACSigner sẽ dùng byte[] của key để ký (HS512 yêu cầu key >= 512 bits để an toàn)
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));

            // Trả ra chuỗi JWT đã được ký, ở dạng compact serialization (chuỗi . ngăn cách 3 phần)
            return jwsObject.serialize();

        } catch (JOSEException e) {
            // Ghi log nếu có lỗi khi ký JWT và ném runtime exception
            log.error("Cannot create token");
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user){
        StringJoiner stringJoiner = new StringJoiner(" ");
        if(!CollectionUtils.isEmpty(user.getRoles())){
//            user.getRoles().forEach(stringJoiner::add);
        }
        return stringJoiner.toString();
    }
}
