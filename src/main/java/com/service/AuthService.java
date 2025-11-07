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
        log.info("Password length: {}", password != null ? password.length() : 0);
        
        try {
            // 1. Kiểm tra user có tồn tại không
            UserAccount user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        log.error("User not found: {}", username);
                        return new RuntimeException("User not found");
                    });
            
            log.info("User found: {}", user.getUsername());
            log.info("User enabled: {}", user.getEnabled());
            log.info("User locked: {}", !user.getAccountNonLocked());
            log.info("User roles: {}", user.getRoles().size());
            log.info("Stored password hash: {}", user.getPassword().substring(0, 20) + "...");
            
            // 2. Kiểm tra password thủ công trước
            boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
            log.info("Manual password check: {}", passwordMatches);
            
            if (!passwordMatches) {
                log.error("Password does not match for user: {}", username);
                throw new BadCredentialsException("Invalid password");
            }
            
            // 3. Kiểm tra trạng thái tài khoản
            if (!user.getEnabled()) {
                log.error("User account is disabled: {}", username);
                throw new RuntimeException("User account is disabled");
            }
            
            if (!user.getAccountNonLocked()) {
                log.error("User account is locked: {}", username);
                throw new RuntimeException("User account is locked");
            }
            
            // 4. Authenticate với Spring Security
            log.info("Attempting Spring Security authentication...");
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            
            log.info("Authentication successful: {}", authentication.isAuthenticated());
            log.info("Authorities: {}", authentication.getAuthorities());
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 5. Generate tokens
            String accessToken = jwtTokenProvider.generateToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(username);
            
            log.info("Tokens generated successfully");
            
            // 6. Update user
            user.setLastLoginAt(LocalDateTime.now());
            user.setFailedLoginAttempts(0);
            user.setRefreshToken(refreshToken);
            userRepository.save(user);
            
            log.info("User updated successfully");
            
            // 7. Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("tokenType", "Bearer");
            response.put("username", user.getUsername());
            response.put("fullName", user.getFullName());
            response.put("email", user.getEmail());
            response.put("roles", user.getRoles());
            
            log.info("=== LOGIN SUCCESS ===");
            return response;
            
        } catch (BadCredentialsException e) {
            log.error("Bad credentials for user: {}", username);
            log.error("Exception: {}", e.getMessage());
            
            updateFailedAttempts(username);
            throw new RuntimeException("Invalid username or password");
            
        } catch (Exception e) {
            log.error("Login exception for user: {}", username);
            log.error("Exception type: {}", e.getClass().getName());
            log.error("Exception message: {}", e.getMessage());
            log.error("Stack trace: ", e);
            
            updateFailedAttempts(username);
            throw new RuntimeException("Invalid username or password");
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
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.existsByEmail(userAccount.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        String rawPassword = userAccount.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        log.info("Password encoded: {}", encodedPassword.substring(0, 20) + "...");
        
        userAccount.setPassword(encodedPassword);
        
        Role role = roleRepository.findByName(roleName != null ? roleName : Role.READER)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        
        userAccount.addRole(role);
        userAccount.setEnabled(true);
        userAccount.setAccountNonLocked(true);
        userAccount.setFailedLoginAttempts(0);
        
        UserAccount savedUser = userRepository.save(userAccount);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User registered successfully");
        response.put("username", savedUser.getUsername());
        response.put("email", savedUser.getEmail());
        
        log.info("=== REGISTER SUCCESS ===");
        return response;
    }
    
    public Map<String, Object> refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }
        
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        UserAccount user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new RuntimeException("Refresh token does not match");
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
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setRefreshToken(null);
        userRepository.save(user);
        
        SecurityContextHolder.clearContext();
        log.info("User logged out: {}", username);
    }
    
    public Map<String, Object> changePassword(String username, String oldPassword, String newPassword) {
        UserAccount user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Password changed successfully");
        
        log.info("Password changed for user: {}", username);
        return response;
    }
}