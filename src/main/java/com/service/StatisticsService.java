package com.service;

import com.repository.BookRepository;
import com.repository.BorrowRepository;
import com.repository.PenaltyRepository;
import com.repository.ReaderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StatisticsService {
    
    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;
    private final BorrowRepository borrowRepository;
    private final PenaltyRepository penaltyRepository;
    
    /**
     * Lấy thống kê tổng quan
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalBooks", bookRepository.count());
        stats.put("totalReaders", readerRepository.count());
        stats.put("totalBorrowTickets", borrowRepository.count());
        stats.put("totalPenalties", penaltyRepository.count());
        
        stats.put("activeReaders", readerRepository.findByStatus(com.model.Reader.ReaderStatus.ACTIVE).size());
        stats.put("borrowedBooks", borrowRepository.findByStatus(com.model.BorrowTicket.BorrowStatus.BORROWED).size());
        stats.put("overdueTickets", borrowRepository.findOverdueTickets(LocalDate.now()).size());
        stats.put("unpaidPenalties", penaltyRepository.findUnpaidPenalties().size());
        
        stats.put("timestamp", System.currentTimeMillis());
        
        return stats;
    }
    
    /**
     * Thống kê sách theo danh mục
     */
    public List<Map<String, Object>> getBookStatsByCategory() {
        List<String> categories = bookRepository.findAllCategories();
        
        return categories.stream().map(category -> {
            Map<String, Object> stat = new HashMap<>();
            stat.put("category", category);
            stat.put("bookCount", bookRepository.findByCategory(category).size());
            stat.put("totalQuantity", bookRepository.findByCategory(category).stream()
                    .mapToInt(book -> book.getTotalQuantity())
                    .sum());
            stat.put("availableQuantity", bookRepository.findByCategory(category).stream()
                    .mapToInt(book -> book.getAvailableQuantity())
                    .sum());
            return stat;
        }).collect(Collectors.toList());
    }
    
    /**
     * Top sách được mượn nhiều nhất
     */
    public List<Map<String, Object>> getTopBorrowedBooks(int limit) {
        var allBorrows = borrowRepository.findAll();
        
        Map<Long, Long> bookBorrowCount = allBorrows.stream()
                .collect(Collectors.groupingBy(
                    bt -> bt.getBook().getId(),
                    Collectors.counting()
                ));
        
        return bookBorrowCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    var book = bookRepository.findById(entry.getKey()).orElse(null);
                    if (book == null) return null;
                    
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("bookId", book.getId());
                    stat.put("bookCode", book.getBookCode());
                    stat.put("title", book.getTitle());
                    stat.put("author", book.getAuthor());
                    stat.put("borrowCount", entry.getValue());
                    return stat;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * Top độc giả mượn sách nhiều nhất
     */
    public List<Map<String, Object>> getTopActiveReaders(int limit) {
        var allBorrows = borrowRepository.findAll();
        
        Map<Long, Long> readerBorrowCount = allBorrows.stream()
                .collect(Collectors.groupingBy(
                    bt -> bt.getReader().getId(),
                    Collectors.counting()
                ));
        
        return readerBorrowCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    var reader = readerRepository.findById(entry.getKey()).orElse(null);
                    if (reader == null) return null;
                    
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("readerId", reader.getId());
                    stat.put("readerCode", reader.getReaderCode());
                    stat.put("fullName", reader.getFullName());
                    stat.put("borrowCount", entry.getValue());
                    
                    long activeBorrows = borrowRepository.countActiveBorrowsByReaderId(reader.getId());
                    stat.put("activeBorrows", activeBorrows);
                    
                    return stat;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * Thống kê mượn trả theo tháng
     */
    public List<Map<String, Object>> getBorrowStatsByMonth(int year) {
        List<Map<String, Object>> stats = new ArrayList<>();
        
        for (int month = 1; month <= 12; month++) {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.plusMonths(1).minusDays(1);
            
            var borrowsInMonth = borrowRepository.findByBorrowDateBetween(startDate, endDate);
            
            Map<String, Object> stat = new HashMap<>();
            stat.put("month", month);
            stat.put("year", year);
            stat.put("totalBorrows", borrowsInMonth.size());
            stat.put("returned", borrowsInMonth.stream()
                    .filter(bt -> bt.getStatus() == com.model.BorrowTicket.BorrowStatus.RETURNED)
                    .count());
            stat.put("borrowed", borrowsInMonth.stream()
                    .filter(bt -> bt.getStatus() == com.model.BorrowTicket.BorrowStatus.BORROWED)
                    .count());
            
            stats.add(stat);
        }
        
        return stats;
    }
    
    /**
     * Thống kê phạt
     */
    public Map<String, Object> getPenaltyStats() {
        var allPenalties = penaltyRepository.findAll();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPenalties", allPenalties.size());
        stats.put("unpaidCount", penaltyRepository.findUnpaidPenalties().size());
        stats.put("paidCount", penaltyRepository.findByPaymentStatus(
            com.model.Penalty.PaymentStatus.PAID).size());
        
        BigDecimal totalAmount = allPenalties.stream()
                .map(com.model.Penalty::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalAmount", totalAmount);
        
        BigDecimal unpaidAmount = penaltyRepository.findUnpaidPenalties().stream()
                .map(com.model.Penalty::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("unpaidAmount", unpaidAmount);
        
        BigDecimal paidAmount = penaltyRepository.findByPaymentStatus(
            com.model.Penalty.PaymentStatus.PAID).stream()
                .map(com.model.Penalty::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("paidAmount", paidAmount);
        
        Map<String, Long> penaltyByType = allPenalties.stream()
                .collect(Collectors.groupingBy(
                    p -> p.getPenaltyType().toString(),
                    Collectors.counting()
                ));
        stats.put("penaltyByType", penaltyByType);
        
        return stats;
    }
    
    /**
     * Thống kê độc giả theo trạng thái
     */
    public Map<String, Object> getReaderStats() {
        var allReaders = readerRepository.findAll();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalReaders", allReaders.size());
        
        Map<String, Long> readersByStatus = allReaders.stream()
                .collect(Collectors.groupingBy(
                    r -> r.getStatus().toString(),
                    Collectors.counting()
                ));
        stats.put("readersByStatus", readersByStatus);
        
        Map<String, Long> readersByGender = allReaders.stream()
                .collect(Collectors.groupingBy(
                    com.model.Reader::getGender,
                    Collectors.counting()
                ));
        stats.put("readersByGender", readersByGender);
        
        long readersExpiringSoon = allReaders.stream()
                .filter(r -> r.getExpiryDate().isBefore(LocalDate.now().plusMonths(1)))
                .filter(r -> r.getExpiryDate().isAfter(LocalDate.now()))
                .count();
        stats.put("readersExpiringSoon", readersExpiringSoon);
        
        return stats;
    }
    
    /**
     * Báo cáo tổng hợp theo khoảng thời gian
     */
    public Map<String, Object> getReportByDateRange(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        
        var borrowsInRange = borrowRepository.findByBorrowDateBetween(startDate, endDate);
        
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("totalBorrows", borrowsInRange.size());
        
        long returned = borrowsInRange.stream()
                .filter(bt -> bt.getStatus() == com.model.BorrowTicket.BorrowStatus.RETURNED)
                .count();
        report.put("returned", returned);
        
        long borrowed = borrowsInRange.stream()
                .filter(bt -> bt.getStatus() == com.model.BorrowTicket.BorrowStatus.BORROWED)
                .count();
        report.put("borrowed", borrowed);
        
        long overdue = borrowsInRange.stream()
                .filter(com.model.BorrowTicket::isOverdue)
                .count();
        report.put("overdue", overdue);
        
        Set<Long> uniqueReaders = borrowsInRange.stream()
                .map(bt -> bt.getReader().getId())
                .collect(Collectors.toSet());
        report.put("uniqueReaders", uniqueReaders.size());
        
        Set<Long> uniqueBooks = borrowsInRange.stream()
                .map(bt -> bt.getBook().getId())
                .collect(Collectors.toSet());
        report.put("uniqueBooks", uniqueBooks.size());
        
        return report;
    }
}