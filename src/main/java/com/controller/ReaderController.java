package com.controller;

import com.model.Reader;
import com.service.ReaderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/readers")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ReaderController {
    
    private final ReaderService readerService;
    
    @PostMapping
    public ResponseEntity<?> createReader(@Valid @RequestBody Reader reader) {
        try {
            Reader createdReader = readerService.createReader(reader);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdReader);
        } catch (Exception e) {
            log.error("Lỗi khi tạo độc giả: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReader(@PathVariable Long id, @Valid @RequestBody Reader reader) {
        try {
            Reader updatedReader = readerService.updateReader(id, reader);
            return ResponseEntity.ok(updatedReader);
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật độc giả: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReader(@PathVariable Long id) {
        try {
            readerService.deleteReader(id);
            return ResponseEntity.ok(createSuccessResponse("Xóa độc giả thành công"));
        } catch (Exception e) {
            log.error("Lỗi khi xóa độc giả: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Reader>> getAllReaders() {
        List<Reader> readers = readerService.getAllReaders();
        return ResponseEntity.ok(readers);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getReaderById(@PathVariable Long id) {
        try {
            Reader reader = readerService.getReaderById(id);
            return ResponseEntity.ok(reader);
        } catch (Exception e) {
            log.error("Lỗi khi lấy độc giả: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Reader>> searchReaders(@RequestParam String keyword) {
        List<Reader> readers = readerService.searchReaders(keyword);
        return ResponseEntity.ok(readers);
    }
    
    @PutMapping("/{id}/renew")
    public ResponseEntity<?> renewReaderCard(@PathVariable Long id, @RequestParam(required = false) Integer months) {
        try {
            Reader reader = readerService.renewReaderCard(id, months);
            return ResponseEntity.ok(reader);
        } catch (Exception e) {
            log.error("Lỗi khi gia hạn thẻ: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
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
}