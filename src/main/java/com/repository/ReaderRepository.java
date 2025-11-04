package com.repository;

import com.model.Reader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReaderRepository extends JpaRepository<Reader, Long> {
    
    Optional<Reader> findByReaderCode(String readerCode);
    
    Optional<Reader> findByEmail(String email);
    
    Optional<Reader> findByPhoneNumber(String phoneNumber);
    
    List<Reader> findByStatus(Reader.ReaderStatus status);
    
    @Query("SELECT r FROM Reader r WHERE " +
           "LOWER(r.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.readerCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "r.phoneNumber LIKE CONCAT('%', :keyword, '%')")
    List<Reader> searchReaders(@Param("keyword") String keyword);
    
    boolean existsByReaderCode(String readerCode);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhoneNumber(String phoneNumber);
}