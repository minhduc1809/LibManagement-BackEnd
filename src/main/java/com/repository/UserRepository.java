package com.repository;

import com.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserAccount, Long> {
    
    // ✅ FIX: Eager load roles để tránh lazy loading exception
    @Query("SELECT DISTINCT u FROM UserAccount u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<UserAccount> findByUsername(@Param("username") String username);
    
    Optional<UserAccount> findByEmail(String email);
    
    Optional<UserAccount> findByRefreshToken(String refreshToken);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<UserAccount> findByEnabled(Boolean enabled);
    
    @Query("SELECT u FROM UserAccount u JOIN u.roles r WHERE r.name = :roleName")
    List<UserAccount> findByRoleName(@Param("roleName") String roleName);
    
    @Query("SELECT u FROM UserAccount u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<UserAccount> searchUsers(@Param("keyword") String keyword);
}