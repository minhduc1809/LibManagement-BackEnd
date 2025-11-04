package com.controller;

import com.model.BorrowTicket;
import com.service.BorrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/borrows")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class BorrowController {
    
    private final BorrowService borrowService;
    
    /**
     * Tạo phiếu mượn sách mới
     */
    @PostMapping
    public ResponseEntity<?> createBorrowTicket(@RequestBody BorrowRequest request) {
        try {
            BorrowTicket borrowTicket = borrowService.createBorrowTicket(
                request.getReaderId(),
                request.getBookId(),
                request.getQuantity(),
                request.getBorrowDays()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(borrowTicket);
        } catch (Exception e) {
            log.error("Lỗi khi tạo phiếu mượn: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Trả sách
     */
    @PutMapping("/{id}/return")
    public ResponseEntity<?> returnBook(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {
        try {
            String returnedTo = request != null ? request.get("returnedTo") : null;
            BorrowTicket borrowTicket = borrowService.returnBook(id, returnedTo);
            return ResponseEntity.ok(borrowTicket);
        } catch (Exception e) {
            log.error("Lỗi khi trả sách: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Gia hạn phiếu mượn
     */
    @PutMapping("/{id}/renew")
    public ResponseEntity<?> renewBorrowTicket(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Integer> request) {
        try {
            Integer additionalDays = request != null ? request.get("additionalDays") : null;
            BorrowTicket borrowTicket = borrowService.renewBorrowTicket(id, additionalDays);
            return ResponseEntity.ok(borrowTicket);
        } catch (Exception e) {
            log.error("Lỗi khi gia hạn phiếu mượn: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Báo mất sách
     */
    @PutMapping("/{id}/report-lost")
    public ResponseEntity<?> reportLostBook(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {
        try {
            String processedBy = request != null ? request.get("processedBy") : null;
            BorrowTicket borrowTicket = borrowService.reportLostBook(id, processedBy);
            return ResponseEntity.ok(borrowTicket);
        } catch (Exception e) {
            log.error("Lỗi khi báo mất sách: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Lấy tất cả phiếu mượn
     */
    @GetMapping
    public ResponseEntity<List<BorrowTicket>> getAllBorrowTickets() {
        List<BorrowTicket> borrowTickets = borrowService.getAllBorrowTickets();
        return ResponseEntity.ok(borrowTickets);
    }
    
    /**
     * Lấy chi tiết phiếu mượn theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getBorrowTicketById(@PathVariable Long id) {
        try {
            BorrowTicket borrowTicket = borrowService.getBorrowTicketById(id);
            return ResponseEntity.ok(borrowTicket);
        } catch (Exception e) {
            log.error("Lỗi khi lấy phiếu mượn: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Lấy danh sách phiếu mượn của một độc giả
     */
    @GetMapping("/reader/{readerId}")
    public ResponseEntity<List<BorrowTicket>> getBorrowTicketsByReader(@PathVariable Long readerId) {
        List<BorrowTicket> borrowTickets = borrowService.getBorrowTicketsByReaderId(readerId);
        return ResponseEntity.ok(borrowTickets);
    }
    
    /**
     * Lấy danh sách phiếu mượn quá hạn
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<BorrowTicket>> getOverdueTickets() {
        List<BorrowTicket> overdueTickets = borrowService.getOverdueTickets();
        return ResponseEntity.ok(overdueTickets);
    }
    
    /**
     * Tìm kiếm phiếu mượn
     */
    @GetMapping("/search")
    public ResponseEntity<List<BorrowTicket>> searchBorrowTickets(@RequestParam String keyword) {
        List<BorrowTicket> borrowTickets = borrowService.searchBorrowTickets(keyword);
        return ResponseEntity.ok(borrowTickets);
    }
    
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    // DTO Classes
    @lombok.Data
    public static class BorrowRequest {
        private Long readerId;
        private Long bookId;
        private Integer quantity;
        private Integer borrowDays;
    }
}