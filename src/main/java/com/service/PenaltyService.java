package com.service;

import com.model.Penalty;
import com.repository.PenaltyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PenaltyService {
    
    private final PenaltyRepository penaltyRepository;
    
    /**
     * Thanh toán phạt
     */
    public Penalty payPenalty(Long penaltyId, String processedBy) {
        Penalty penalty = penaltyRepository.findById(penaltyId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phạt với ID: " + penaltyId));
        
        if (penalty.getPaymentStatus() == Penalty.PaymentStatus.PAID) {
            throw new RuntimeException("Phạt đã được thanh toán trước đó");
        }
        
        penalty.setPaymentStatus(Penalty.PaymentStatus.PAID);
        penalty.setPaymentDate(LocalDate.now());
        penalty.setProcessedBy(processedBy);
        
        log.info("Thanh toán phạt ID: {} - Số tiền: {}", penaltyId, penalty.getAmount());
        return penaltyRepository.save(penalty);
    }
    
    /**
     * Miễn phạt
     */
    public Penalty waivePenalty(Long penaltyId, String processedBy, String reason) {
        Penalty penalty = penaltyRepository.findById(penaltyId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phạt với ID: " + penaltyId));
        
        if (penalty.getPaymentStatus() == Penalty.PaymentStatus.PAID) {
            throw new RuntimeException("Không thể miễn phạt đã thanh toán");
        }
        
        penalty.setPaymentStatus(Penalty.PaymentStatus.WAIVED);
        penalty.setPaymentDate(LocalDate.now());
        penalty.setProcessedBy(processedBy);
        penalty.setReason(penalty.getReason() + " - Miễn phạt: " + reason);
        
        log.info("Miễn phạt ID: {} - Người xử lý: {}", penaltyId, processedBy);
        return penaltyRepository.save(penalty);
    }
    
    /**
     * Lấy tất cả phạt
     */
    @Transactional(readOnly = true)
    public List<Penalty> getAllPenalties() {
        return penaltyRepository.findAll();
    }
    
    /**
     * Lấy chi tiết phạt theo ID
     */
    @Transactional(readOnly = true)
    public Penalty getPenaltyById(Long id) {
        return penaltyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phạt với ID: " + id));
    }
    
    /**
     * Lấy danh sách phạt chưa thanh toán
     */
    @Transactional(readOnly = true)
    public List<Penalty> getUnpaidPenalties() {
        return penaltyRepository.findUnpaidPenalties();
    }
    
    /**
     * Lấy danh sách phạt theo độc giả
     */
    @Transactional(readOnly = true)
    public List<Penalty> getPenaltiesByReaderId(Long readerId) {
        return penaltyRepository.findByReaderId(readerId);
    }
    
    /**
     * Lấy danh sách phạt theo loại
     */
    @Transactional(readOnly = true)
    public List<Penalty> getPenaltiesByType(Penalty.PenaltyType penaltyType) {
        return penaltyRepository.findByPenaltyType(penaltyType);
    }
    
    /**
     * Lấy danh sách phạt theo trạng thái thanh toán
     */
    @Transactional(readOnly = true)
    public List<Penalty> getPenaltiesByPaymentStatus(Penalty.PaymentStatus paymentStatus) {
        return penaltyRepository.findByPaymentStatus(paymentStatus);
    }
}