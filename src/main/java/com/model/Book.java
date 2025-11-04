package com.model;

import lombok.*;
import java.time.LocalDate;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Mã sách không được để trống")
    @Column(unique = true, nullable = false, length = 50)
    private String bookCode;
    
    @NotBlank(message = "Tên sách không được để trống")
    @Column(nullable = false, length = 200)
    private String title;
    
    @NotBlank(message = "Tác giả không được để trống")
    @Column(nullable = false, length = 100)
    private String author;
    
    @Column(length = 100)
    private String publisher;
    
    @Column(name = "publish_year")
    private Integer publishYear;
    
    @Column(length = 50)
    private String category;
    
    @Column(length = 20)
    private String isbn;
    
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    @Column(nullable = false)
    private Integer totalQuantity;
    
    @Column(nullable = false)
    private Integer availableQuantity;
    
    @Column(length = 500)
    private String description;
    
    @Column(length = 255)
    private String imageUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookStatus status = BookStatus.AVAILABLE;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;
    
    @Column(name = "updated_at")
    private LocalDate updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
        updatedAt = LocalDate.now();
        if (availableQuantity == null) {
            availableQuantity = totalQuantity;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDate.now();
    }
    
    public enum BookStatus {
        AVAILABLE,      // Còn sách
        OUT_OF_STOCK,   // Hết sách
        MAINTENANCE     // Bảo trì
    }
}
