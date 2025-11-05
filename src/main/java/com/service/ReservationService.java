package com.service;

import com.model.Book;
import com.model.Reader;
import com.model.Reservation;
import com.repository.BookRepository;
import com.repository.ReaderRepository;
import com.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReservationService {
    
    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;
    
    private static final int MAX_RESERVATIONS_PER_READER = 3;
    private static final int RESERVATION_VALIDITY_DAYS = 7;
    private static final int PICKUP_VALIDITY_DAYS = 3;
    
    /**
     * Tạo đặt trước sách
     */
    public Reservation createReservation(Long readerId, Long bookId, String notes) {
        Reader reader = readerRepository.findById(readerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy độc giả với ID: " + readerId));
        
        if (reader.getStatus() != Reader.ReaderStatus.ACTIVE) {
            throw new RuntimeException("Độc giả không ở trạng thái hoạt động");
        }
        
        long pendingReservations = reservationRepository.countPendingReservationsByReaderId(readerId);
        if (pendingReservations >= MAX_RESERVATIONS_PER_READER) {
            throw new RuntimeException("Độc giả đã đặt trước tối đa " + MAX_RESERVATIONS_PER_READER + " quyển sách");
        }
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách với ID: " + bookId));
        
        // Kiểm tra xem độc giả đã đặt trước sách này chưa
        List<Reservation> existingReservations = reservationRepository.findByReaderIdAndStatus(
                readerId, Reservation.ReservationStatus.PENDING);
        
        boolean alreadyReserved = existingReservations.stream()
                .anyMatch(r -> r.getBook().getId().equals(bookId));
        
        if (alreadyReserved) {
            throw new RuntimeException("Độc giả đã đặt trước sách này rồi");
        }
        
        String reservationCode = generateReservationCode();
        LocalDate expiryDate = LocalDate.now().plusDays(RESERVATION_VALIDITY_DAYS);
        
        Reservation reservation = Reservation.builder()
                .reservationCode(reservationCode)
                .reader(reader)
                .book(book)
                .reservationDate(LocalDateTime.now())
                .expiryDate(expiryDate)
                .status(Reservation.ReservationStatus.PENDING)
                .notes(notes)
                .build();
        
        log.info("Tạo đặt trước: {} cho độc giả: {} - Sách: {}", 
                reservationCode, reader.getFullName(), book.getTitle());
        return reservationRepository.save(reservation);
    }
    
    /**
     * Đánh dấu sách đã có sẵn để lấy
     */
    public Reservation markAsAvailable(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đặt trước với ID: " + reservationId));
        
        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể đánh dấu sẵn sàng cho đặt trước đang chờ");
        }
        
        reservation.setStatus(Reservation.ReservationStatus.AVAILABLE);
        reservation.setNotifiedAt(LocalDateTime.now());
        reservation.setExpiryDate(LocalDate.now().plusDays(PICKUP_VALIDITY_DAYS));
        
        log.info("Đánh dấu sách sẵn sàng cho đặt trước: {}", reservation.getReservationCode());
        return reservationRepository.save(reservation);
    }
    
    /**
     * Hoàn thành đặt trước (đã mượn sách)
     */
    public Reservation fulfillReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đặt trước với ID: " + reservationId));
        
        if (reservation.getStatus() != Reservation.ReservationStatus.AVAILABLE) {
            throw new RuntimeException("Sách chưa sẵn sàng để lấy");
        }
        
        reservation.setStatus(Reservation.ReservationStatus.FULFILLED);
        reservation.setFulfilledAt(LocalDateTime.now());
        
        log.info("Hoàn thành đặt trước: {}", reservation.getReservationCode());
        return reservationRepository.save(reservation);
    }
    
    /**
     * Hủy đặt trước
     */
    public Reservation cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đặt trước với ID: " + reservationId));
        
        if (reservation.getStatus() == Reservation.ReservationStatus.FULFILLED) {
            throw new RuntimeException("Không thể hủy đặt trước đã hoàn thành");
        }
        
        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        
        log.info("Hủy đặt trước: {}", reservation.getReservationCode());
        return reservationRepository.save(reservation);
    }
    
    /**
     * Xử lý đặt trước hết hạn
     */
    public void processExpiredReservations() {
        List<Reservation> expiredPending = reservationRepository.findExpiredReservations(LocalDate.now());
        List<Reservation> expiredAvailable = reservationRepository.findExpiredAvailableReservations(LocalDate.now());
        
        for (Reservation reservation : expiredPending) {
            reservation.setStatus(Reservation.ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);
            log.info("Đặt trước hết hạn (chờ): {}", reservation.getReservationCode());
        }
        
        for (Reservation reservation : expiredAvailable) {
            reservation.setStatus(Reservation.ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);
            log.info("Đặt trước hết hạn (sẵn sàng): {}", reservation.getReservationCode());
        }
    }
    
    private String generateReservationCode() {
        String prefix = "RS";
        String timestamp = String.valueOf(System.currentTimeMillis());
        return prefix + timestamp.substring(timestamp.length() - 10);
    }
    
    @Transactional(readOnly = true)
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đặt trước với ID: " + id));
    }
    
    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByReaderId(Long readerId) {
        return reservationRepository.findByReaderId(readerId);
    }
    
    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByBookId(Long bookId) {
        return reservationRepository.findByBookId(bookId);
    }
    
    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByStatus(Reservation.ReservationStatus status) {
        return reservationRepository.findByStatus(status);
    }
}