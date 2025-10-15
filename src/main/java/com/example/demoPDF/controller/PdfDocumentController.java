package com.example.demoPDF.controller;

import com.example.demoPDF.dto.ApiResponse;
import com.example.demoPDF.dto.FileUploadResponse;
import com.example.demoPDF.dto.PdfDocumentDto;
import com.example.demoPDF.dto.PdfUploadRequest;
import com.example.demoPDF.service.PdfDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/pdf")  // Changed from "/api/pdf" to match your frontend
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class PdfDocumentController {

    private final PdfDocumentService pdfDocumentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "productCode", required = false) String productCode,
            @RequestParam(value = "edition", required = false) String edition,
            @RequestParam(value = "publicationDate", required = false) String publicationDateStr,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "createdBy", required = false) String createdBy) {

        log.info("=== UPLOAD REQUEST RECEIVED ===");
        log.info("File: {}", file.getOriginalFilename());
        log.info("File size: {}", file.getSize());
        log.info("Content type: {}", file.getContentType());
        log.info("Title: {}", title);
        log.info("Product Code: {}", productCode);
        log.info("Edition: {}", edition);
        log.info("Publication Date String: {}", publicationDateStr);
        log.info("Notes: {}", notes);
        log.info("Created By: {}", createdBy);

        try {
            // Validate file
            if (file.isEmpty()) {
                log.error("File is empty");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File is empty"));
            }

            if (!"application/pdf".equals(file.getContentType())) {
                log.error("Invalid file type: {}", file.getContentType());
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File must be a PDF"));
            }

            // Parse publication date
            LocalDate publicationDate = null;
            if (publicationDateStr != null && !publicationDateStr.trim().isEmpty()) {
                try {
                    publicationDate = LocalDate.parse(publicationDateStr);
                    log.info("Parsed publication date: {}", publicationDate);
                } catch (Exception e) {
                    log.warn("Could not parse publication date: {}", publicationDateStr, e);
                }
            }

            // Create upload request
            PdfUploadRequest request = new PdfUploadRequest();
            request.setTitle(title != null && !title.trim().isEmpty() ? title : file.getOriginalFilename().replace(".pdf", ""));
            request.setProductCode(productCode);
            request.setEdition(edition);
            request.setPublicationDate(publicationDate);
            request.setNotes(notes);
            request.setCreatedBy(createdBy != null ? createdBy : "system");

            log.info("Created upload request: {}", request);

            // Upload document
            log.info("Calling service to upload document...");
            PdfDocumentDto savedDocument = pdfDocumentService.uploadDocument(file, request);
            log.info("Document uploaded successfully with ID: {}", savedDocument.getId());

            // Create response
            FileUploadResponse response = new FileUploadResponse(
                    savedDocument.getId(),
                    savedDocument.getFilename(),
                    savedDocument.getFileSize(),
                    savedDocument.getPageCount(),
                    "Document uploaded successfully"
            );

            return ResponseEntity.ok(ApiResponse.success("Document uploaded successfully", response));

        } catch (Exception e) {
            log.error("=== UPLOAD ERROR ===", e);
            log.error("Error type: {}", e.getClass().getSimpleName());
            log.error("Error message: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("Root cause: {}", e.getCause().getMessage());
            }

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to upload document: " + e.getMessage()));
        }
    }

    // Test endpoint to verify controller is working
    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> testEndpoint() {
        log.info("Test endpoint called");
        return ResponseEntity.ok(ApiResponse.success("Controller is working!"));
    }

    @GetMapping("/documents")
    public ResponseEntity<ApiResponse<List<PdfDocumentDto>>> getAllDocuments() {
        try {
            log.debug("Fetching all documents");
            List<PdfDocumentDto> documents = pdfDocumentService.getAllDocuments();
            return ResponseEntity.ok(ApiResponse.success(documents));
        } catch (Exception e) {
            log.error("Error fetching documents: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch documents"));
        }
    }

    // ... rest of your controller methods remain the same
}
