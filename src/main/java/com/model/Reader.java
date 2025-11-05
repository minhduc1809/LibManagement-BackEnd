package com.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "readers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reader {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Mã độc giả không được để trống")
    @Column(unique = true, nullable = false, length = 50)
    private String readerCode;
    
    @NotBlank(message = "Họ tên không được để trống")
    @Column(nullable = false, length = 100)
    private String fullName;
    
    @NotNull(message = "Ngày sinh không được để trống")
    @Column(nullable = false)
    private LocalDate dateOfBirth;
    
    @NotBlank(message = "Giới tính không được để trống")
    @Column(nullable = false, length = 10)
    private String gender;
    
    @Column(length = 200)
    private String address;
    
    @Email(message = "Email không hợp lệ")
    @Column(length = 100)
    private String email;
    
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
    @Column(length = 15)
    private String phoneNumber;
    
    @Column(length = 20)
    private String identityCard;
    
    @Column(nullable = false)
    private LocalDate issueDate;
    
    @Column(nullable = false)
    private LocalDate expiryDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReaderStatus status = ReaderStatus.ACTIVE;
    
    @Column(length = 255)
    private String avatarUrl;
    
    @OneToMany(mappedBy = "reader", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<BorrowTicket> borrowTickets;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;
    
    @Column(name = "updated_at")
    private LocalDate updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
        updatedAt = LocalDate.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDate.now();
    }
    
    public enum ReaderStatus {
        ACTIVE,
        EXPIRED,
        SUSPENDED,
        BLOCKED
    }
}