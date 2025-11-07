package com.controller;

import com.model.UserAccount;
import com.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AdminController {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Reset password cho user (chỉ dùng trong development)
     * Endpoint này không nên expose trong production
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            UserAccount user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found: " + request.getUsername()));
            
            String encodedPassword = passwordEncoder.encode(request.getNewPassword());
            user.setPassword(encodedPassword);
            userRepository.save(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Password reset successfully");
            response.put("username", user.getUsername());
            
            log.info("Password reset for user: {}", request.getUsername());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Reset password error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Tạo tài khoản admin mới với password đơn giản
     */
    @PostMapping("/create-default-admin")
    public ResponseEntity<?> createDefaultAdmin() {
        try {
            if (userRepository.existsByUsername("admin")) {
                return ResponseEntity.badRequest().body(
                    createErrorResponse("Admin account already exists"));
            }
            
            UserAccount admin = UserAccount.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("123456"))
                    .email("admin@library.com")
                    .fullName("Administrator")
                    .phoneNumber("0900000000")
                    .enabled(true)
                    .accountNonLocked(true)
                    .failedLoginAttempts(0)
                    .build();
            
            userRepository.save(admin);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Admin account created");
            response.put("username", "admin");
            response.put("password", "123456");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Create admin error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Test endpoint để kiểm tra password encoding
     */
    @PostMapping("/test-password")
    public ResponseEntity<?> testPassword(@RequestBody TestPasswordRequest request) {
        String encoded = passwordEncoder.encode(request.getPassword());
        boolean matches = passwordEncoder.matches(request.getPassword(), encoded);
        
        Map<String, Object> response = new HashMap<>();
        response.put("rawPassword", request.getPassword());
        response.put("encodedPassword", encoded);
        response.put("matches", matches);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Kiểm tra password của user
     */
    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyPassword(@RequestBody VerifyPasswordRequest request) {
        try {
            UserAccount user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
            
            Map<String, Object> response = new HashMap<>();
            response.put("username", request.getUsername());
            response.put("passwordMatches", matches);
            response.put("storedPasswordHash", user.getPassword());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
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
    
    /**
     * Debug endpoint - Xem chi tiết user và test password
     */
    @GetMapping("/debug-user/{username}")
    public ResponseEntity<?> debugUser(@PathVariable String username) {
        try {
            UserAccount user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("fullName", user.getFullName());
            response.put("enabled", user.getEnabled());
            response.put("accountNonLocked", user.getAccountNonLocked());
            response.put("failedLoginAttempts", user.getFailedLoginAttempts());
            response.put("passwordHash", user.getPassword());
            response.put("roles", user.getRoles().stream()
                    .map(r -> r.getName())
                    .toList());
            
            // Test với password "123456"
            boolean test123456 = passwordEncoder.matches("123456", user.getPassword());
            response.put("matches_123456", test123456);
            
            // Test với password "password123"
            boolean testPassword123 = passwordEncoder.matches("password123", user.getPassword());
            response.put("matches_password123", testPassword123);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Cập nhật password trực tiếp trong database
     */
    @PostMapping("/update-password-direct")
    public ResponseEntity<?> updatePasswordDirect(@RequestBody UpdatePasswordRequest request) {
        try {
            UserAccount user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Encode password mới
            String encodedPassword = passwordEncoder.encode(request.getNewPassword());
            
            // Update trực tiếp
            user.setPassword(encodedPassword);
            user.setEnabled(true);
            user.setAccountNonLocked(true);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
            
            // Verify ngay
            boolean matches = passwordEncoder.matches(request.getNewPassword(), encodedPassword);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("username", user.getUsername());
            response.put("newPasswordHash", encodedPassword);
            response.put("verificationMatches", matches);
            response.put("message", "Password updated and verified");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    @Data
    public static class ResetPasswordRequest {
        private String username;
        private String newPassword;
    }
    
    @Data
    public static class TestPasswordRequest {
        private String password;
    }
    
    @Data
    public static class VerifyPasswordRequest {
        private String username;
        private String password;
    }
    
    @Data
    public static class UpdatePasswordRequest {
        private String username;
        private String newPassword;
    }
}