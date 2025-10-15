package com.example.demoPDF.repository;

import com.example.demoPDF.entity.PdfDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PdfDocumentRepository extends JpaRepository<PdfDocument, Long> {

    // Find all active documents
    List<PdfDocument> findByIsActiveTrueOrderByUploadDateDesc();

    // Find active document by ID
    Optional<PdfDocument> findByIdAndIsActiveTrue(Long id);

    // Search documents by various fields
    @Query("SELECT p FROM PdfDocument p WHERE p.isActive = true AND " +
            "(LOWER(p.title) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
            "LOWER(p.filename) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
            "LOWER(p.productCode) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
            "LOWER(p.edition) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
            "LOWER(p.notes) LIKE LOWER(CONCAT('%', :searchQuery, '%'))) " +
            "ORDER BY p.uploadDate DESC")
    List<PdfDocument> searchDocuments(@Param("searchQuery") String searchQuery);

    // Find by product code
    List<PdfDocument> findByProductCodeAndIsActiveTrueOrderByUploadDateDesc(String productCode);

    // Find by edition
    List<PdfDocument> findByEditionAndIsActiveTrueOrderByUploadDateDesc(String edition);

    // Count total active documents
    long countByIsActiveTrue();
}