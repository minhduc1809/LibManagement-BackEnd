package com.security;

import com.model.Role;
import com.model.UserAccount;
import com.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user by username: {}", username);
        
        UserAccount user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        log.info("User found: {}, roles size: {}", user.getUsername(), user.getRoles() != null ? user.getRoles().size() : 0);
        
        // ✅ FIX: Lấy authorities TRONG transaction để tránh lazy loading issue
        Collection<? extends GrantedAuthority> authorities = getAuthorities(user);
        
        log.info("Authorities loaded: {}", authorities);
        
        UserDetails userDetails = User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!user.getAccountNonLocked())
                .credentialsExpired(false)
                .disabled(!user.getEnabled())
                .build();
        
        log.info("UserDetails created successfully for: {}", username);
        return userDetails;
    }
    
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        log.info("Loading user by id: {}", id);
        
        UserAccount user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        
        Collection<? extends GrantedAuthority> authorities = getAuthorities(user);
        
        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!user.getAccountNonLocked())
                .credentialsExpired(false)
                .disabled(!user.getEnabled())
                .build();
    }
    
    // ✅ FIX: Method riêng để xử lý authorities an toàn
    private Collection<? extends GrantedAuthority> getAuthorities(UserAccount user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        try {
            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                // Tạo List mới từ roles để tránh ConcurrentModificationException
                List<Role> rolesList = new ArrayList<>(user.getRoles());
                
                for (Role role : rolesList) {
                    if (role != null && role.getName() != null) {
                        authorities.add(new SimpleGrantedAuthority(role.getName()));
                        log.debug("Added authority: {}", role.getName());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error loading authorities for user {}: {}", user.getUsername(), e.getMessage());
            // Trả về list rỗng thay vì throw exception
        }
        
        return authorities;
    }
}