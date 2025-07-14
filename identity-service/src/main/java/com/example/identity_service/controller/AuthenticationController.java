package com.example.identity_service.controller;

import com.example.identity_service.dto.request.ApiResponse;
import com.example.identity_service.dto.request.AuthenticationRequest;
import com.example.identity_service.dto.request.IntrospecRequest;
import com.example.identity_service.dto.response.AuthenticationResponse;
import com.example.identity_service.dto.response.IntrospecResponse;
import com.example.identity_service.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

/**
 * Controller xử lý các API liên quan đến xác thực (authentication).
 *
 * ➤ @RestController = @Controller + @ResponseBody (trả JSON)
 * ➤ @RequestMapping("/auth") nghĩa là mọi API trong class này đều bắt đầu bằng `/auth`
 */
@RestController
@RequestMapping("/auth")

/**
 * Tự động tạo constructor chứa tất cả các field có final hoặc @NonNull.
 * ➤ Giúp Spring Boot DI (Dependency Injection) thông qua constructor.
 * ➤ Không cần @Autowired.
 */
@RequiredArgsConstructor

/**
 * Cài đặt mặc định cho tất cả field:
 * ➤ private (bảo mật tốt hơn)
 * ➤ final (bất biến, không cho thay đổi sau khi khởi tạo)
 * ➤ Điều này kết hợp tốt với @RequiredArgsConstructor
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

    /**
     * Service xử lý logic xác thực (login, introspect, ...)
     * ➤ Vì có @RequiredArgsConstructor + final => Spring sẽ inject constructor tự động
     */
    AuthenticationService authenticationService;

    /**
     * API POST /auth/token
     * ➤ Nhận vào AuthenticationRequest (username/password)
     * ➤ Trả ra token JWT và các thông tin khác dưới dạng ApiResponse<AuthenticationResponse>
     */
    @PostMapping("/token")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest authenticationRequest) {
        AuthenticationResponse result = authenticationService.authenticate(authenticationRequest);

        /**
         * Sử dụng builder pattern để tạo ra ApiResponse
         * ➤ Generic <AuthenticationResponse> được chỉ rõ trong builder
         * ➤ Builder giúp tạo object rõ ràng, mạch lạc hơn constructor thông thường
         */
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)                          // nội dung trả về chính
                .message("wtf is going on here")         // thông điệp kèm theo (tùy chỉnh)
                .build();                                // kết thúc builder và trả object
    }

    /**
     * API POST /auth/introspect
     * ➤ Nhận vào access token, kiểm tra hợp lệ, trả kết quả decode token
     * ➤ Có thể quăng ParseException và JOSEException từ JWT library
     */
    @PostMapping("/introspect")
    ApiResponse<IntrospecResponse> authenticate(@RequestBody IntrospecRequest introspecRequest)
            throws ParseException, JOSEException {

        IntrospecResponse result = authenticationService.introspect(introspecRequest);

        return ApiResponse.<IntrospecResponse>builder()
                .result(result)
                .message("introspect")
                .build();
    }
}
