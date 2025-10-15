package com.example.demoPDF.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PdfDocumentDto {
    private Long id;
    private String title;
    private String filename;
    private Long fileSize;
    private Integer pageCount;
    private String productCode;
    private String edition;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate publicationDate;

    private String notes;
    private String contentType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadDate;

    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedDate;
}