package com.model;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "penalties")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Penalty {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "borrow_ticket_id", nullable = false)
    private BorrowTicket borrowTicket;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PenaltyType penaltyType;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(length = 500)
    private String reason;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;
    
    private LocalDate paymentDate;
    
    @Column(length = 100)
    private String processedBy;
    
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
    
    public enum PenaltyType {
        OVERDUE,    // Phạt trễ hạn
        LOST,       // Phạt mất sách
        DAMAGED     // Phạt hư hỏng
    }
    
    public enum PaymentStatus {
        UNPAID,     // Chưa thanh toán
        PAID,       // Đã thanh toán
        WAIVED      // Được miễn
    }
}