package com.service;

import com.model.Role;
import com.model.UserAccount;
import com.repository.RoleRepository;
import com.repository.UserRepository;
import com.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
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
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            String accessToken = jwtTokenProvider.generateToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(username);
            
            UserAccount user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            user.setLastLoginAt(LocalDateTime.now());
            user.setFailedLoginAttempts(0);
            user.setRefreshToken(refreshToken);
            userRepository.save(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("tokenType", "Bearer");
            response.put("username", user.getUsername());
            response.put("fullName", user.getFullName());
            response.put("email", user.getEmail());
            response.put("roles", user.getRoles());
            
            log.info("User logged in successfully: {}", username);
            return response;
            
        } catch (Exception e) {
            UserAccount user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
                if (user.getFailedLoginAttempts() >= 5) {
                    user.setAccountNonLocked(false);
                    log.warn("Account locked due to too many failed login attempts: {}", username);
                }
                userRepository.save(user);
            }
            
            log.error("Login failed for user: {}", username);
            throw new RuntimeException("Invalid username or password");
        }
    }
    
    public Map<String, Object> register(UserAccount userAccount, String roleName) {
        if (userRepository.existsByUsername(userAccount.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.existsByEmail(userAccount.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        userAccount.setPassword(passwordEncoder.encode(userAccount.getPassword()));
        
        Role role = roleRepository.findByName(roleName != null ? roleName : Role.READER)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        
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
        
        log.info("New user registered: {}", savedUser.getUsername());
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