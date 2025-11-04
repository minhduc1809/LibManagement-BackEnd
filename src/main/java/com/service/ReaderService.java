package com.service;

import com.model.Reader;
import com.repository.ReaderRepository;
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
public class ReaderService {
    
    private final ReaderRepository readerRepository;
    
    private static final int DEFAULT_CARD_VALIDITY_MONTHS = 12;
    
    public Reader createReader(Reader reader) {
        // Kiểm tra mã độc giả đã tồn tại
        if (readerRepository.existsByReaderCode(reader.getReaderCode())) {
            throw new RuntimeException("Mã độc giả đã tồn tại: " + reader.getReaderCode());
        }
        
        // Kiểm tra email đã tồn tại
        if (reader.getEmail() != null && readerRepository.existsByEmail(reader.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng: " + reader.getEmail());
        }
        
        // Kiểm tra số điện thoại đã tồn tại
        if (reader.getPhoneNumber() != null && readerRepository.existsByPhoneNumber(reader.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại đã được sử dụng: " + reader.getPhoneNumber());
        }
        
        // Thiết lập ngày phát hành và hết hạn nếu chưa có
        if (reader.getIssueDate() == null) {
            reader.setIssueDate(LocalDate.now());
        }
        if (reader.getExpiryDate() == null) {
            reader.setExpiryDate(LocalDate.now().plusMonths(DEFAULT_CARD_VALIDITY_MONTHS));
        }
        
        log.info("Tạo độc giả mới: {}", reader.getFullName());
        return readerRepository.save(reader);
    }
    
    public Reader updateReader(Long id, Reader readerDetails) {
        Reader reader = readerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy độc giả với ID: " + id));
        
        reader.setFullName(readerDetails.getFullName());
        reader.setDateOfBirth(readerDetails.getDateOfBirth());
        reader.setGender(readerDetails.getGender());
        reader.setAddress(readerDetails.getAddress());
        reader.setEmail(readerDetails.getEmail());
        reader.setPhoneNumber(readerDetails.getPhoneNumber());
        reader.setIdentityCard(readerDetails.getIdentityCard());
        
        log.info("Cập nhật độc giả: {}", reader.getFullName());
        return readerRepository.save(reader);
    }
    
    public void deleteReader(Long id) {
        Reader reader = readerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy độc giả với ID: " + id));
        
        log.info("Xóa độc giả: {}", reader.getFullName());
        readerRepository.delete(reader);
    }
    
    @Transactional(readOnly = true)
    public List<Reader> getAllReaders() {
        return readerRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Reader getReaderById(Long id) {
        return readerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy độc giả với ID: " + id));
    }
    
    @Transactional(readOnly = true)
    public List<Reader> searchReaders(String keyword) {
        return readerRepository.searchReaders(keyword);
    }
    
    public Reader renewReaderCard(Long id, Integer months) {
        Reader reader = readerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy độc giả với ID: " + id));
        
        if (months == null || months <= 0) {
            months = DEFAULT_CARD_VALIDITY_MONTHS;
        }
        
        reader.setExpiryDate(reader.getExpiryDate().plusMonths(months));
        reader.setStatus(Reader.ReaderStatus.ACTIVE);
        
        log.info("Gia hạn thẻ độc giả: {} thêm {} tháng", reader.getFullName(), months);
        return readerRepository.save(reader);
    }
}