package com.example.identity_service.configuration;

import com.example.identity_service.entity.User;
import com.example.identity_service.enums.Role;
import com.example.identity_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

/**
 * Cấu hình để khởi tạo dữ liệu khi ứng dụng Spring Boot khởi động lần đầu.
 * Cụ thể là tạo user "admin" mặc định nếu chưa tồn tại.
 */
@Slf4j // Cung cấp log.info, log.error,... dùng cho việc log ra console
@Configuration // Đánh dấu đây là class cấu hình được Spring quét và thực thi khi app chạy
@RequiredArgsConstructor // Tự động tạo constructor chứa các field final (cho phép Spring DI)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true) // Các field mặc định là private final
public class ApplicationInitConfig {

    // Spring sẽ tự inject bean PasswordEncoder từ SecurityConfig
    PasswordEncoder passwordEncoder;

    /**
     * Đăng ký một bean ApplicationRunner
     * ApplicationRunner là functional interface của Spring Boot, được chạy sau khi ứng dụng khởi động thành công
     * Mục đích ở đây là khởi tạo user admin mặc định vào cơ sở dữ liệu nếu chưa có
     *
     * @param userRepository - được inject bởi Spring, dùng để truy xuất người dùng trong DB
     * @return ApplicationRunner để chạy logic khởi tạo admin user
     */
    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository) {
        return args -> {
            // Kiểm tra nếu user admin chưa tồn tại trong database
            if (userRepository.findByUsername("admin").isEmpty()) {

                // Tạo set chứa role ADMIN (ở đây là string "ADMIN")
                var roles = new HashSet<String>();
                roles.add(Role.ADMIN.name()); // Lấy tên Enum = "ADMIN"

                // Tạo đối tượng user admin với mật khẩu đã mã hóa
                User user = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin")) // encode mật khẩu bằng BCrypt (hoặc cấu hình trong SecurityConfig)
                        .roles(roles)
                        .build();

                // Lưu user vào database
                userRepository.save(user);

                // Log ra console để biết có tạo user admin
                log.info("Admin user created with default credentials: username=admin, password=admin");
            } else {
                // Nếu đã có user admin thì log cho biết
                log.info("Admin user already exists");
            }
        };
    }
}
