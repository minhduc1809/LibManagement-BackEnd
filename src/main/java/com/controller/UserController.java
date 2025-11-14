package com.controller;

import com.model.UserAccount;
import com.repository.UserRepository;
import com.repository.RoleRepository;
import com.model.Role;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('LIBRARIAN')")
public class UserController {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Lấy danh sách tất cả users
     */
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            List<UserAccount> users = userRepository.findAll();
            
            List<Map<String, Object>> userList = users.stream().map(user -> {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("email", user.getEmail());
                userInfo.put("fullName", user.getFullName());
                userInfo.put("phoneNumber", user.getPhoneNumber());
                userInfo.put("enabled", user.getEnabled());
                userInfo.put("accountNonLocked", user.getAccountNonLocked());
                userInfo.put("failedLoginAttempts", user.getFailedLoginAttempts());
                userInfo.put("lastLoginAt", user.getLastLoginAt());
                userInfo.put("roles", user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()));
                return userInfo;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(userList);
            
        } catch (Exception e) {
            log.error("Get all users error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Lấy thông tin chi tiết user
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            UserAccount user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("fullName", user.getFullName());
            userInfo.put("phoneNumber", user.getPhoneNumber());
            userInfo.put("enabled", user.getEnabled());
            userInfo.put("accountNonLocked", user.getAccountNonLocked());
            userInfo.put("failedLoginAttempts", user.getFailedLoginAttempts());
            userInfo.put("lastLoginAt", user.getLastLoginAt());
            userInfo.put("createdAt", user.getCreatedAt());
            userInfo.put("updatedAt", user.getUpdatedAt());
            userInfo.put("roles", user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList()));
            
            return ResponseEntity.ok(userInfo);
            
        } catch (Exception e) {
            log.error("Get user error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

 
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        try {
            if (userRepository.existsByUsername(request.getUsername())) {
                return ResponseEntity.badRequest().body(
                    createErrorResponse("Tên đăng nhập đã tồn tại"));
            }
            
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body(
                    createErrorResponse("Email đã được sử dụng"));
            }
            
            String roleName = (request.getRoleName() != null && !request.getRoleName().isEmpty()) 
                ? request.getRoleName() : Role.READER;
            
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò: " + roleName));
            
            UserAccount user = UserAccount.builder()
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .email(request.getEmail())
                    .fullName(request.getFullName())
                    .phoneNumber(request.getPhoneNumber())
                    .enabled(true)
                    .accountNonLocked(true)
                    .failedLoginAttempts(0)
                    .build();
            
            user.addRole(role);
            UserAccount savedUser = userRepository.save(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tạo tài khoản thành công");
            response.put("userId", savedUser.getId());
            response.put("username", savedUser.getUsername());
            response.put("email", savedUser.getEmail());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Create user error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id, 
                                          @RequestBody ResetPasswordRequest request) {
        try {
            UserAccount user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
            
            String encodedPassword = passwordEncoder.encode(request.getNewPassword());
            user.setPassword(encodedPassword);
            user.setFailedLoginAttempts(0);
            user.setAccountNonLocked(true);
            userRepository.save(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đặt lại mật khẩu thành công");
            response.put("username", user.getUsername());
            
            log.info("Password reset for user: {}", user.getUsername());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Reset password error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

 
    @PutMapping("/{id}/lock-status")
    public ResponseEntity<?> updateLockStatus(@PathVariable Long id, 
                                              @RequestBody LockStatusRequest request) {
        try {
            UserAccount user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
            
            user.setAccountNonLocked(request.getAccountNonLocked());
            if (request.getAccountNonLocked()) {
                user.setFailedLoginAttempts(0);
            }
            userRepository.save(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", request.getAccountNonLocked() ? 
                "Mở khóa tài khoản thành công" : "Khóa tài khoản thành công");
            response.put("username", user.getUsername());
            response.put("accountNonLocked", user.getAccountNonLocked());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Update lock status error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }


    @PutMapping("/{id}/enabled-status")
    public ResponseEntity<?> updateEnabledStatus(@PathVariable Long id, 
                                                 @RequestBody EnabledStatusRequest request) {
        try {
            UserAccount user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
            
            user.setEnabled(request.getEnabled());
            userRepository.save(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", request.getEnabled() ? 
                "Kích hoạt tài khoản thành công" : "Vô hiệu hóa tài khoản thành công");
            response.put("username", user.getUsername());
            response.put("enabled", user.getEnabled());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Update enabled status error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            UserAccount user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
            
            String username = user.getUsername();
            userRepository.delete(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Xóa tài khoản thành công");
            response.put("username", username);
            
            log.info("User deleted: {}", username);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Delete user error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String keyword) {
        try {
            List<UserAccount> users = userRepository.searchUsers(keyword);
            
            List<Map<String, Object>> userList = users.stream().map(user -> {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("email", user.getEmail());
                userInfo.put("fullName", user.getFullName());
                userInfo.put("phoneNumber", user.getPhoneNumber());
                userInfo.put("enabled", user.getEnabled());
                userInfo.put("accountNonLocked", user.getAccountNonLocked());
                userInfo.put("roles", user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()));
                return userInfo;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(userList);
            
        } catch (Exception e) {
            log.error("Search users error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    @Data
    public static class CreateUserRequest {
        private String username;
        private String password;
        private String email;
        private String fullName;
        private String phoneNumber;
        private String roleName;
    }
    
    @Data
    public static class ResetPasswordRequest {
        private String newPassword;
    }
    
    @Data
    public static class LockStatusRequest {
        private Boolean accountNonLocked;
    }
    
    @Data
    public static class EnabledStatusRequest {
        private Boolean enabled;
    }
}