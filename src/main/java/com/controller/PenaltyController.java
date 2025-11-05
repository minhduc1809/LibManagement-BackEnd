package com.controller;

import com.model.Penalty;
import com.service.PenaltyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/penalties")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PenaltyController {
    
    private final PenaltyService penaltyService;
    
    /**
     * Thanh toán phạt
     */
    @PutMapping("/{id}/pay")
    public ResponseEntity<?> payPenalty(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {
        try {
            String processedBy = request != null ? request.get("processedBy") : null;
            Penalty penalty = penaltyService.payPenalty(id, processedBy);
            return ResponseEntity.ok(penalty);
        } catch (Exception e) {
            log.error("Lỗi khi thanh toán phạt: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Miễn phạt
     */
    @PutMapping("/{id}/waive")
    public ResponseEntity<?> waivePenalty(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String processedBy = request.get("processedBy");
            String reason = request.get("reason");
            Penalty penalty = penaltyService.waivePenalty(id, processedBy, reason);
            return ResponseEntity.ok(penalty);
        } catch (Exception e) {
            log.error("Lỗi khi miễn phạt: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Lấy tất cả phạt
     */
    @GetMapping
    public ResponseEntity<List<Penalty>> getAllPenalties() {
        List<Penalty> penalties = penaltyService.getAllPenalties();
        return ResponseEntity.ok(penalties);
    }
    
    /**
     * Lấy chi tiết phạt theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPenaltyById(@PathVariable Long id) {
        try {
            Penalty penalty = penaltyService.getPenaltyById(id);
            return ResponseEntity.ok(penalty);
        } catch (Exception e) {
            log.error("Lỗi khi lấy phạt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Lấy danh sách phạt chưa thanh toán
     */
    @GetMapping("/unpaid")
    public ResponseEntity<List<Penalty>> getUnpaidPenalties() {
        List<Penalty> penalties = penaltyService.getUnpaidPenalties();
        return ResponseEntity.ok(penalties);
    }
    
    /**
     * Lấy danh sách phạt theo độc giả
     */
    @GetMapping("/reader/{readerId}")
    public ResponseEntity<List<Penalty>> getPenaltiesByReader(@PathVariable Long readerId) {
        List<Penalty> penalties = penaltyService.getPenaltiesByReaderId(readerId);
        return ResponseEntity.ok(penalties);
    }
    
    /**
     * Lấy danh sách phạt theo loại
     */
    @GetMapping("/type/{penaltyType}")
    public ResponseEntity<List<Penalty>> getPenaltiesByType(@PathVariable Penalty.PenaltyType penaltyType) {
        List<Penalty> penalties = penaltyService.getPenaltiesByType(penaltyType);
        return ResponseEntity.ok(penalties);
    }
    
    /**
     * Lấy danh sách phạt theo trạng thái thanh toán
     */
    @GetMapping("/payment-status/{paymentStatus}")
    public ResponseEntity<List<Penalty>> getPenaltiesByPaymentStatus(
            @PathVariable Penalty.PaymentStatus paymentStatus) {
        List<Penalty> penalties = penaltyService.getPenaltiesByPaymentStatus(paymentStatus);
        return ResponseEntity.ok(penalties);
    }
    
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}