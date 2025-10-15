//package com.example.demoPDF.entity;
//
//import jakarta.persistence.*;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "bookmarks")
//public class Bookmark {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "user_id", nullable = false)
//    private String userId; // You can use IP address or session ID for simple implementation
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "pdf_document_id", nullable = false)
//    private PdfDocument pdfDocument;
//
//    @Column(name = "page_number")
//    private Integer pageNumber;
//
//    @Column(name = "bookmark_name")
//    private String bookmarkName;
//
//    @Column(name = "created_date")
//    private LocalDateTime createdDate;
//
//    @PrePersist
//    public void prePersist() {
//        createdDate = LocalDateTime.now();
//    }
//
//    // Constructors
//    public Bookmark() {}
//
//    public Bookmark(String userId, PdfDocument pdfDocument, Integer pageNumber, String bookmarkName) {
//        this.userId = userId;
//        this.pdfDocument = pdfDocument;
//        this.pageNumber = pageNumber;
//        this.bookmarkName = bookmarkName;
//    }
//
//    // Getters and Setters
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//
//    public String getUserId() { return userId; }
//    public void setUserId(String userId) { this.userId = userId; }
//
//    public PdfDocument getPdfDocument() { return pdfDocument; }
//    public void setPdfDocument(PdfDocument pdfDocument) { this.pdfDocument = pdfDocument; }
//
//    public Integer getPageNumber() { return pageNumber; }
//    public void setPageNumber(Integer pageNumber) { this.pageNumber = pageNumber; }
//
//    public String getBookmarkName() { return bookmarkName; }
//    public void setBookmarkName(String bookmarkName) { this.bookmarkName = bookmarkName; }
//
//    public LocalDateTime getCreatedDate() { return createdDate; }
//    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
//}
