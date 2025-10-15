package com.example.demoPDF.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pdf_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PdfDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "filename", nullable = false, length = 255)
    private String filename;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "product_code", length = 100)
    private String productCode;

    @Column(name = "edition", length = 100)
    private String edition;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @Column(name = "notes", columnDefinition = "NVARCHAR(MAX)")
    private String notes;

    @Column(name = "content_type", length = 100)
    private String contentType = "application/pdf";

    @Column(name = "upload_date", nullable = false)
    @CreationTimestamp
    private LocalDateTime uploadDate;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_date")
    @UpdateTimestamp
    private LocalDateTime updatedDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
