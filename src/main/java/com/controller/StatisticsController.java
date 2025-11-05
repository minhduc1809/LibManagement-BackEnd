package com.controller;

import com.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class StatisticsController {
    
    private final StatisticsService statisticsService;
    
    /**
     * Lấy thống kê tổng quan dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        log.info("Lấy thống kê dashboard");
        Map<String, Object> stats = statisticsService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Thống kê sách theo danh mục
     */
    @GetMapping("/books/by-category")
    public ResponseEntity<List<Map<String, Object>>> getBookStatsByCategory() {
        log.info("Thống kê sách theo danh mục");
        List<Map<String, Object>> stats = statisticsService.getBookStatsByCategory();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Top sách được mượn nhiều nhất
     */
    @GetMapping("/books/top-borrowed")
    public ResponseEntity<List<Map<String, Object>>> getTopBorrowedBooks(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Lấy top {} sách được mượn nhiều nhất", limit);
        List<Map<String, Object>> stats = statisticsService.getTopBorrowedBooks(limit);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Top độc giả mượn sách nhiều nhất
     */
    @GetMapping("/readers/top-active")
    public ResponseEntity<List<Map<String, Object>>> getTopActiveReaders(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Lấy top {} độc giả mượn nhiều nhất", limit);
        List<Map<String, Object>> stats = statisticsService.getTopActiveReaders(limit);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Thống kê mượn trả theo tháng
     */
    @GetMapping("/borrows/by-month")
    public ResponseEntity<List<Map<String, Object>>> getBorrowStatsByMonth(
            @RequestParam(defaultValue = "2024") int year) {
        log.info("Thống kê mượn trả theo tháng năm {}", year);
        List<Map<String, Object>> stats = statisticsService.getBorrowStatsByMonth(year);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Thống kê phạt
     */
    @GetMapping("/penalties")
    public ResponseEntity<Map<String, Object>> getPenaltyStats() {
        log.info("Thống kê phạt");
        Map<String, Object> stats = statisticsService.getPenaltyStats();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Thống kê độc giả
     */
    @GetMapping("/readers")
    public ResponseEntity<Map<String, Object>> getReaderStats() {
        log.info("Thống kê độc giả");
        Map<String, Object> stats = statisticsService.getReaderStats();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Báo cáo theo khoảng thời gian
     */
    @GetMapping("/report/date-range")
    public ResponseEntity<Map<String, Object>> getReportByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Báo cáo từ {} đến {}", startDate, endDate);
        Map<String, Object> report = statisticsService.getReportByDateRange(startDate, endDate);
        return ResponseEntity.ok(report);
    }
}