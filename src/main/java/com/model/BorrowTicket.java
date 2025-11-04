package com.model;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "borrow_tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowTicket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String ticketCode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reader_id", nullable = false)
    private Reader reader;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    
    @Column(nullable = false)
    private LocalDate borrowDate;
    
    @Column(nullable = false)
    private LocalDate dueDate;
    
    private LocalDate returnDate;
    
    @Column(nullable = false)
    private Integer quantity = 1;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BorrowStatus status = BorrowStatus.BORROWED;
    
    @Column(length = 100)
    private String returnedTo;
    
    @OneToOne(mappedBy = "borrowTicket", cascade = CascadeType.ALL)
    private Penalty penalty;
    
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
    
    public boolean isOverdue() {
        if (returnDate != null) {
            return returnDate.isAfter(dueDate);
        }
        return LocalDate.now().isAfter(dueDate) && status == BorrowStatus.BORROWED;
    }
    
    public long getOverdueDays() {
        if (!isOverdue()) return 0;
        
        LocalDate compareDate = returnDate != null ? returnDate : LocalDate.now();
        return ChronoUnit.DAYS.between(dueDate, compareDate);
    }
    
    public enum BorrowStatus {
        BORROWED,   // Đang mượn
        RETURNED,   // Đã trả
        OVERDUE,    // Quá hạn
        LOST        // Mất sách
    }
}