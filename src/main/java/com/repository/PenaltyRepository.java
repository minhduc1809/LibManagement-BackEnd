package com.repository;

import com.model.Penalty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PenaltyRepository extends JpaRepository<Penalty, Long> {
    
    Optional<Penalty> findByBorrowTicketId(Long borrowTicketId);
    
    List<Penalty> findByPaymentStatus(Penalty.PaymentStatus paymentStatus);
    
    List<Penalty> findByPenaltyType(Penalty.PenaltyType penaltyType);
    
    @Query("SELECT p FROM Penalty p WHERE p.paymentStatus = 'UNPAID'")
    List<Penalty> findUnpaidPenalties();
    
    @Query("SELECT p FROM Penalty p JOIN p.borrowTicket bt WHERE bt.reader.id = :readerId")
    List<Penalty> findByReaderId(@Param("readerId") Long readerId);
}
