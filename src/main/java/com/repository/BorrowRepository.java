package com.repository;

import com.model.BorrowTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BorrowRepository extends JpaRepository<BorrowTicket, Long> {
    
    List<BorrowTicket> findByReaderId(Long readerId);
    
    List<BorrowTicket> findByBookId(Long bookId);
    
    List<BorrowTicket> findByStatus(BorrowTicket.BorrowStatus status);
    
    @Query("SELECT COUNT(bt) FROM BorrowTicket bt WHERE bt.reader.id = :readerId AND bt.status = 'BORROWED'")
    long countActiveBorrowsByReaderId(@Param("readerId") Long readerId);
    
    @Query("SELECT bt FROM BorrowTicket bt WHERE bt.dueDate < :currentDate AND bt.status = 'BORROWED'")
    List<BorrowTicket> findOverdueTickets(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT bt FROM BorrowTicket bt WHERE " +
           "LOWER(bt.ticketCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(bt.reader.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(bt.book.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<BorrowTicket> searchBorrowTickets(@Param("keyword") String keyword);
    
    List<BorrowTicket> findByBorrowDateBetween(LocalDate startDate, LocalDate endDate);
}