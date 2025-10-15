package com.example.demoPDF.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Data
public class PdfUploadRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 100, message = "Product code must not exceed 100 characters")
    private String productCode;

    @Size(max = 100, message = "Edition must not exceed 100 characters")
    private String edition;

    private LocalDate publicationDate;

    @Size(max = 4000, message = "Notes must not exceed 4000 characters")
    private String notes;

    private String createdBy;
}