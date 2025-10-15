//package com.example.demoPDF.repository;
//
//import com.example.demoPDF.entity.Bookmark;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.stereotype.Repository;
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
//
//    List<Bookmark> findByUserIdOrderByCreatedDateDesc(String userId);
//
//    @Query("SELECT b FROM Bookmark b WHERE b.userId = :userId AND b.pdfDocument.id = :documentId")
//    Optional<Bookmark> findByUserIdAndDocumentId(String userId, Long documentId);
//
//    void deleteByUserIdAndPdfDocumentId(String userId, Long documentId);
//}