package com.service;

import com.model.Role;
import com.model.UserAccount;
import com.repository.RoleRepository;
import com.repository.UserRepository;
import com.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    
    public Map<String, Object> login(String username, String password) {
        log.info("=== LOGIN ATTEMPT ===");
        log.info("Username: {}", username);
        
        try {
            // Kiểm tra user có tồn tại không
            UserAccount user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        log.error("User not found: {}", username);
                        return new RuntimeException("Tên đăng nhập hoặc mật khẩu không đúng");
                    });
            
            log.info("User found: {}", user.getUsername());
            log.info("User enabled: {}", user.getEnabled());
            log.info("User locked: {}", !user.getAccountNonLocked());
            
            // Kiểm tra trạng thái tài khoản
            if (!user.getEnabled()) {
                log.error("User account is disabled: {}", username);
                throw new RuntimeException("Tài khoản đã bị vô hiệu hóa");
            }
            
            if (!user.getAccountNonLocked()) {
                log.error("User account is locked: {}", username);
                throw new RuntimeException("Tài khoản đã bị khóa do đăng nhập sai quá nhiều lần");
            }
            
            // Authenticate với Spring Security
            log.info("Attempting Spring Security authentication...");
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            
            log.info("Authentication successful: {}", authentication.isAuthenticated());
            log.info("Authorities: {}", authentication.getAuthorities());
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Generate tokens
            String accessToken = jwtTokenProvider.generateToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(username);
            
            log.info("Tokens generated successfully");
            
            // Update user
            user.setLastLoginAt(LocalDateTime.now());
            user.setFailedLoginAttempts(0);
            user.setRefreshToken(refreshToken);
            userRepository.save(user);
            
            log.info("User updated successfully");
            
            // Determine role
            String userRole = "READER";
            if (user.hasRole(Role.LIBRARIAN)) {
                userRole = "LIBRARIAN";
            }
            
            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("tokenType", "Bearer");
            response.put("username", user.getUsername());
            response.put("fullName", user.getFullName());
            response.put("email", user.getEmail());
            response.put("role", userRole);
            response.put("roles", user.getRoles());
            
            log.info("=== LOGIN SUCCESS ===");
            return response;
            
        } catch (BadCredentialsException e) {
            log.error("Bad credentials for user: {}", username);
            updateFailedAttempts(username);
            throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không đúng");
            
        } catch (Exception e) {
            log.error("Login exception for user: {}", username);
            log.error("Exception message: {}", e.getMessage());
            updateFailedAttempts(username);
            throw new RuntimeException("Đăng nhập thất bại: " + e.getMessage());
        }
    }
    
    private void updateFailedAttempts(String username) {
        try {
            UserAccount user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
                log.warn("Failed login attempts for {}: {}", username, user.getFailedLoginAttempts());
                
                if (user.getFailedLoginAttempts() >= 5) {
                    user.setAccountNonLocked(false);
                    log.warn("Account locked due to too many failed attempts: {}", username);
                }
                userRepository.save(user);
            }
        } catch (Exception e) {
            log.error("Error updating failed attempts: {}", e.getMessage());
        }
    }
    
    public Map<String, Object> register(UserAccount userAccount, String roleName) {
        log.info("=== REGISTER ATTEMPT ===");
        log.info("Username: {}", userAccount.getUsername());
        
        if (userRepository.existsByUsername(userAccount.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }
        
        if (userRepository.existsByEmail(userAccount.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }
        
        String encodedPassword = passwordEncoder.encode(userAccount.getPassword());
        userAccount.setPassword(encodedPassword);
        
        // Default role là READER nếu không chỉ định
        String finalRoleName = (roleName != null && !roleName.isEmpty()) ? roleName : Role.READER;
        
        Role role = roleRepository.findByName(finalRoleName)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò: " + finalRoleName));
        
        userAccount.addRole(role);
        userAccount.setEnabled(true);
        userAccount.setAccountNonLocked(true);
        userAccount.setFailedLoginAttempts(0);
        
        UserAccount savedUser = userRepository.save(userAccount);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đăng ký tài khoản thành công");
        response.put("username", savedUser.getUsername());
        response.put("email", savedUser.getEmail());
        response.put("role", finalRoleName);
        
        log.info("=== REGISTER SUCCESS ===");
        return response;
    }
    
    public Map<String, Object> refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Refresh token không hợp lệ hoặc đã hết hạn");
        }
        
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        UserAccount user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new RuntimeException("Refresh token không khớp");
        }
        
        String newAccessToken = jwtTokenProvider.generateToken(username);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username);
        
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("accessToken", newAccessToken);
        response.put("refreshToken", newRefreshToken);
        response.put("tokenType", "Bearer");
        
        return response;
    }
    
    public void logout(String username) {
        UserAccount user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        user.setRefreshToken(null);
        userRepository.save(user);
        
        SecurityContextHolder.clearContext();
        log.info("User logged out: {}", username);
    }
    
    public Map<String, Object> changePassword(String username, String oldPassword, String newPassword) {
        UserAccount user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không đúng");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đổi mật khẩu thành công");
        
        log.info("Password changed for user: {}", username);
        return response;
    }
}