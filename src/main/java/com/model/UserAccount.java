package com.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccount {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Username không được để trống")
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    @NotBlank(message = "Password không được để trống")
    @JsonIgnore
    @Column(nullable = false)
    private String password;
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Column(unique = true, nullable = false, length = 100)
    private String email;
    
    @Column(nullable = false, length = 100)
    private String fullName;
    
    @Column(length = 15)
    private String phoneNumber;
    
    // ✅ FIX: Đổi sang EAGER và thêm @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    @ToString.Exclude  // Tránh infinite loop khi toString
    @EqualsAndHashCode.Exclude  // Tránh issue khi compare
    private Set<Role> roles = new HashSet<>();
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean accountNonLocked = true;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;
    
    private LocalDateTime lastLoginAt;
    
    private LocalDateTime passwordChangedAt;
    
    @Column(length = 500)
    private String refreshToken;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (roles == null) {
            roles = new HashSet<>();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public void addRole(Role role) {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.add(role);
    }
    
    public void removeRole(Role role) {
        if (this.roles != null) {
            this.roles.remove(role);
        }
    }
    
    public boolean hasRole(String roleName) {
        if (roles == null) return false;
        return roles.stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }
}