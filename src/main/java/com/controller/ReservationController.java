package com.controller;

import com.model.Reservation;
import com.service.ReservationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ReservationController {
    
    private final ReservationService reservationService;
    
    /**
     * Tạo đặt trước sách
     */
    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody ReservationRequest request) {
        try {
            Reservation reservation = reservationService.createReservation(
                    request.getReaderId(),
                    request.getBookId(),
                    request.getNotes()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
        } catch (Exception e) {
            log.error("Lỗi khi tạo đặt trước: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Đánh dấu sách đã sẵn sàng
     */
    @PutMapping("/{id}/mark-available")
    public ResponseEntity<?> markAsAvailable(@PathVariable Long id) {
        try {
            Reservation reservation = reservationService.markAsAvailable(id);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            log.error("Lỗi khi đánh dấu sẵn sàng: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Hoàn thành đặt trước
     */
    @PutMapping("/{id}/fulfill")
    public ResponseEntity<?> fulfillReservation(@PathVariable Long id) {
        try {
            Reservation reservation = reservationService.fulfillReservation(id);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            log.error("Lỗi khi hoàn thành đặt trước: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Hủy đặt trước
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelReservation(@PathVariable Long id) {
        try {
            Reservation reservation = reservationService.cancelReservation(id);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            log.error("Lỗi khi hủy đặt trước: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Xử lý đặt trước hết hạn
     */
    @PostMapping("/process-expired")
    public ResponseEntity<?> processExpiredReservations() {
        try {
            reservationService.processExpiredReservations();
            return ResponseEntity.ok(createSuccessResponse("Đã xử lý các đặt trước hết hạn"));
        } catch (Exception e) {
            log.error("Lỗi khi xử lý hết hạn: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Lấy tất cả đặt trước
     */
    @GetMapping
    public ResponseEntity<List<Reservation>> getAllReservations() {
        List<Reservation> reservations = reservationService.getAllReservations();
        return ResponseEntity.ok(reservations);
    }
    
    /**
     * Lấy chi tiết đặt trước theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getReservationById(@PathVariable Long id) {
        try {
            Reservation reservation = reservationService.getReservationById(id);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            log.error("Lỗi khi lấy đặt trước: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Lấy đặt trước theo độc giả
     */
    @GetMapping("/reader/{readerId}")
    public ResponseEntity<List<Reservation>> getReservationsByReader(@PathVariable Long readerId) {
        List<Reservation> reservations = reservationService.getReservationsByReaderId(readerId);
        return ResponseEntity.ok(reservations);
    }
    
    /**
     * Lấy đặt trước theo sách
     */
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<Reservation>> getReservationsByBook(@PathVariable Long bookId) {
        List<Reservation> reservations = reservationService.getReservationsByBookId(bookId);
        return ResponseEntity.ok(reservations);
    }
    
    /**
     * Lấy đặt trước theo trạng thái
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Reservation>> getReservationsByStatus(
            @PathVariable Reservation.ReservationStatus status) {
        List<Reservation> reservations = reservationService.getReservationsByStatus(status);
        return ResponseEntity.ok(reservations);
    }
    
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    // DTO Class
    @Data
    public static class ReservationRequest {
        private Long readerId;
        private Long bookId;
        private String notes;
    }
}