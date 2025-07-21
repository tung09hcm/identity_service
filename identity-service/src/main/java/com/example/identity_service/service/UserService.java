package com.example.identity_service.service;

import com.example.identity_service.dto.request.UserCreationRequest;
import com.example.identity_service.dto.request.UserUpdateRequest;
import com.example.identity_service.dto.response.UserResponse;
import com.example.identity_service.entity.User;
import com.example.identity_service.enums.Role;
import com.example.identity_service.exception.AppException;
import com.example.identity_service.exception.ErrorCode;
import com.example.identity_service.mapper.UserMapper;
import com.example.identity_service.repository.RoleRepository;
import com.example.identity_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;

    public UserResponse createUser(UserCreationRequest request){
        log.info("Service: create user");
        if(userRepository.existsByUsername(request.getUsername())){
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        User user = userMapper.toUser(request);

        HashSet<String> roles = new HashSet<>();
        roles.add(Role.USER.name());
//        user.setRoles(roles);

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userMapper.toUserResponse(userRepository.save(user));
    }

    public UserResponse getMyInfo(){
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        User user = userRepository.findByUsername(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }


    /**
     * Chỉ cho phép những user có role là ADMIN được gọi method này.
     *
     * - hasRole('ADMIN'): kiểm tra role của user hiện tại có chứa "ROLE_ADMIN"
     *     + ROLE_ là prefix mặc định do Spring Security thêm vào (xem jwtAuthenticationConverter)
     *
     * - Annotation này được kiểm tra **trước khi method được gọi**
     *
     * - Cần bật @EnableMethodSecurity ở cấu hình để sử dụng được
     */
    @PreAuthorize("hasRole('ADMIN')")
//    @PreAuthorize("hasAuthority(``)")
    public List<UserResponse> getAllUser() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    /**
     * Chỉ cho phép trả về user nếu username của user đó == người hiện tại đang đăng nhập.
     *
     * - @PostAuthorize: kiểm tra **sau khi method thực thi xong**, trước khi trả kết quả cho client.
     *
     * - returnObject: là **giá trị trả về của method này**, ở đây là `UserResponse`.
     *     + Có thể truy cập field của object này như bình thường: `returnObject.username`
     *
     * - authentication.name: là **tên người dùng hiện tại đăng nhập** (thường là username trong token JWT)
     *     + `authentication` là object mặc định được Spring cung cấp trong security context
     *     + bạn không cần khai báo, Spring luôn inject sẵn
     *
     * ➤ Ý nghĩa: chỉ cho trả về user nếu đó là chính mình (không được xem người khác)
     */
    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse getUser(String userId) {
        return userMapper.toUserResponse(
                userRepository.findById(userId)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED))
        );
    }


    public User updateUser(String userId, UserUpdateRequest request){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        request.setPassword(passwordEncoder.encode(request.getPassword()));
        System.out.println("password: " + request.getPassword());

        userMapper.updateUser(user,request);

        var roles = roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roles));

        return userRepository.save(user);

    }

    public User deleteUser(String userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        userRepository.deleteById(userId);
        return user;
    }
}
