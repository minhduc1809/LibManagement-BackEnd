package com.service;

import com.model.Book;
import com.model.BorrowTicket;
import com.model.Penalty;
import com.model.Reader;
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
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BorrowService {
    
    private final BorrowRepository borrowRepository;
    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;
    private final PenaltyRepository penaltyRepository;
    
    private static final int MAX_BORROW_BOOKS = 5;
    private static final int DEFAULT_BORROW_DAYS = 14;
    private static final BigDecimal OVERDUE_FEE_PER_DAY = new BigDecimal("5000");
    
    public BorrowTicket createBorrowTicket(Long readerId, Long bookId, Integer quantity, Integer borrowDays) {
        Reader reader = readerRepository.findById(readerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy độc giả với ID: " + readerId));
        
        if (reader.getStatus() != Reader.ReaderStatus.ACTIVE) {
            throw new RuntimeException("Độc giả không ở trạng thái hoạt động");
        }
        
        if (reader.getExpiryDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Thẻ độc giả đã hết hạn");
        }
        
        long activeBorrows = borrowRepository.countActiveBorrowsByReaderId(readerId);
        if (activeBorrows >= MAX_BORROW_BOOKS) {
            throw new RuntimeException("Độc giả đã mượn tối đa " + MAX_BORROW_BOOKS + " quyển sách");
        }
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách với ID: " + bookId));
        
        if (book.getStatus() != Book.BookStatus.AVAILABLE) {
            throw new RuntimeException("Sách không ở trạng thái có sẵn");
        }
        
        if (quantity == null || quantity <= 0) {
            quantity = 1;
        }
        
        if (book.getAvailableQuantity() < quantity) {
            throw new RuntimeException("Không đủ sách để cho mượn. Còn lại: " + book.getAvailableQuantity());
        }
        
        String ticketCode = generateTicketCode();
        
        if (borrowDays == null || borrowDays <= 0) {
            borrowDays = DEFAULT_BORROW_DAYS;
        }
        LocalDate dueDate = LocalDate.now().plusDays(borrowDays);
        
        BorrowTicket borrowTicket = BorrowTicket.builder()
                .ticketCode(ticketCode)
                .reader(reader)
                .book(book)
                .borrowDate(LocalDate.now())
                .dueDate(dueDate)
                .quantity(quantity)
                .status(BorrowTicket.BorrowStatus.BORROWED)
                .build();
        
        book.setAvailableQuantity(book.getAvailableQuantity() - quantity);
        if (book.getAvailableQuantity() == 0) {
            book.setStatus(Book.BookStatus.OUT_OF_STOCK);
        }
        bookRepository.save(book);
        
        log.info("Tạo phiếu mượn: {} cho độc giả: {} - Sách: {}", ticketCode, reader.getFullName(), book.getTitle());
        return borrowRepository.save(borrowTicket);
    }
    
    public BorrowTicket returnBook(Long borrowTicketId, String returnedTo) {
        BorrowTicket borrowTicket = borrowRepository.findById(borrowTicketId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mượn với ID: " + borrowTicketId));
        
        if (borrowTicket.getStatus() == BorrowTicket.BorrowStatus.RETURNED) {
            throw new RuntimeException("Sách đã được trả trước đó");
        }
        
        borrowTicket.setReturnDate(LocalDate.now());
        borrowTicket.setStatus(BorrowTicket.BorrowStatus.RETURNED);
        borrowTicket.setReturnedTo(returnedTo);
        
        if (borrowTicket.isOverdue()) {
            createOverduePenalty(borrowTicket);
        }
        
        Book book = borrowTicket.getBook();
        book.setAvailableQuantity(book.getAvailableQuantity() + borrowTicket.getQuantity());
        if (book.getAvailableQuantity() > 0 && book.getStatus() == Book.BookStatus.OUT_OF_STOCK) {
            book.setStatus(Book.BookStatus.AVAILABLE);
        }
        bookRepository.save(book);
        
        log.info("Trả sách: {} - Phiếu mượn: {}", book.getTitle(), borrowTicket.getTicketCode());
        return borrowRepository.save(borrowTicket);
    }
    
    public BorrowTicket renewBorrowTicket(Long borrowTicketId, Integer additionalDays) {
        BorrowTicket borrowTicket = borrowRepository.findById(borrowTicketId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mượn với ID: " + borrowTicketId));
        
        if (borrowTicket.getStatus() != BorrowTicket.BorrowStatus.BORROWED) {
            throw new RuntimeException("Chỉ có thể gia hạn phiếu mượn đang hoạt động");
        }
        
        if (borrowTicket.isOverdue()) {
            throw new RuntimeException("Không thể gia hạn phiếu mượn đã quá hạn");
        }
        
        if (additionalDays == null || additionalDays <= 0) {
            additionalDays = DEFAULT_BORROW_DAYS;
        }
        
        borrowTicket.setDueDate(borrowTicket.getDueDate().plusDays(additionalDays));
        
        log.info("Gia hạn phiếu mượn: {} thêm {} ngày", borrowTicket.getTicketCode(), additionalDays);
        return borrowRepository.save(borrowTicket);
    }
    
    public BorrowTicket reportLostBook(Long borrowTicketId, String processedBy) {
        BorrowTicket borrowTicket = borrowRepository.findById(borrowTicketId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mượn với ID: " + borrowTicketId));
        
        borrowTicket.setStatus(BorrowTicket.BorrowStatus.LOST);
        
        Book book = borrowTicket.getBook();
        BigDecimal lostBookFee = new BigDecimal("100000");
        
        Penalty penalty = Penalty.builder()
                .borrowTicket(borrowTicket)
                .penaltyType(Penalty.PenaltyType.LOST)
                .amount(lostBookFee)
                .reason("Mất sách: " + book.getTitle())
                .paymentStatus(Penalty.PaymentStatus.UNPAID)
                .processedBy(processedBy)
                .build();
        
        penaltyRepository.save(penalty);
        
        log.info("Báo mất sách - Phiếu mượn: {}", borrowTicket.getTicketCode());
        return borrowRepository.save(borrowTicket);
    }
    
    private void createOverduePenalty(BorrowTicket borrowTicket) {
        long overdueDays = borrowTicket.getOverdueDays();
        BigDecimal penaltyAmount = OVERDUE_FEE_PER_DAY.multiply(new BigDecimal(overdueDays));
        
        Penalty penalty = Penalty.builder()
                .borrowTicket(borrowTicket)
                .penaltyType(Penalty.PenaltyType.OVERDUE)
                .amount(penaltyAmount)
                .reason("Trả sách trễ " + overdueDays + " ngày")
                .paymentStatus(Penalty.PaymentStatus.UNPAID)
                .build();
        
        penaltyRepository.save(penalty);
        log.info("Tạo phiếu phạt quá hạn: {} - Số tiền: {}", borrowTicket.getTicketCode(), penaltyAmount);
    }
    
    private String generateTicketCode() {
        String prefix = "MT";
        String timestamp = String.valueOf(System.currentTimeMillis());
        return prefix + timestamp.substring(timestamp.length() - 10);
    }
    
    @Transactional(readOnly = true)
    public List<BorrowTicket> getAllBorrowTickets() {
        return borrowRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public BorrowTicket getBorrowTicketById(Long id) {
        return borrowRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mượn với ID: " + id));
    }
    
    @Transactional(readOnly = true)
    public List<BorrowTicket> getBorrowTicketsByReaderId(Long readerId) {
        return borrowRepository.findByReaderId(readerId);
    }
    
    @Transactional(readOnly = true)
    public List<BorrowTicket> getOverdueTickets() {
        return borrowRepository.findOverdueTickets(LocalDate.now());
    }
    
    @Transactional(readOnly = true)
    public List<BorrowTicket> searchBorrowTickets(String keyword) {
        return borrowRepository.searchBorrowTickets(keyword);
    }
}