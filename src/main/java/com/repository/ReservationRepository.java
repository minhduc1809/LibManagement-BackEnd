package com.repository;

import com.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    Optional<Reservation> findByReservationCode(String reservationCode);
    
    List<Reservation> findByReaderId(Long readerId);
    
    List<Reservation> findByBookId(Long bookId);
    
    List<Reservation> findByStatus(Reservation.ReservationStatus status);
    
    @Query("SELECT r FROM Reservation r WHERE r.reader.id = :readerId AND r.status = :status")
    List<Reservation> findByReaderIdAndStatus(@Param("readerId") Long readerId, 
                                               @Param("status") Reservation.ReservationStatus status);
    
    @Query("SELECT r FROM Reservation r WHERE r.book.id = :bookId AND r.status = 'PENDING'")
    List<Reservation> findPendingReservationsByBookId(@Param("bookId") Long bookId);
    
    @Query("SELECT r FROM Reservation r WHERE r.expiryDate < :currentDate AND r.status = 'PENDING'")
    List<Reservation> findExpiredReservations(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT r FROM Reservation r WHERE r.status = 'AVAILABLE' AND r.expiryDate < :currentDate")
    List<Reservation> findExpiredAvailableReservations(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.reader.id = :readerId AND r.status = 'PENDING'")
    long countPendingReservationsByReaderId(@Param("readerId") Long readerId);
}