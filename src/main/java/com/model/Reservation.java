package com.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String reservationCode;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reader_id", nullable = false)
    @JsonIgnoreProperties({"borrowTickets", "hibernateLazyInitializer", "handler"})
    private Reader reader;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Book book;
    
    @Column(nullable = false)
    private LocalDateTime reservationDate;
    
    @Column(nullable = false)
    private LocalDate expiryDate;
    
    private LocalDateTime notifiedAt;
    
    private LocalDateTime fulfilledAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.PENDING;
    
    @Column(length = 500)
    private String notes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate) && status == ReservationStatus.PENDING;
    }
    
    public enum ReservationStatus {
        PENDING,        // Đang chờ
        AVAILABLE,      // Đã có sách, chờ lấy
        FULFILLED,      // Đã hoàn thành (đã mượn)
        CANCELLED,      // Đã hủy
        EXPIRED         // Đã hết hạn
    }
}