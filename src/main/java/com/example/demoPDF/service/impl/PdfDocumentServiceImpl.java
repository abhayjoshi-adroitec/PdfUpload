package com.example.demoPDF.service.impl;

import com.example.demoPDF.dto.PdfDocumentDto;
import com.example.demoPDF.dto.PdfUploadRequest;
import com.example.demoPDF.entity.PdfDocument;
import com.example.demoPDF.repository.PdfDocumentRepository;
import com.example.demoPDF.service.PdfDocumentService;
import com.example.demoPDF.service.FileStorageService;
import com.example.demoPDF.util.PdfUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PdfDocumentServiceImpl implements PdfDocumentService {

    private final PdfDocumentRepository repository;
    private final FileStorageService fileStorageService;

    @Override
    public PdfDocumentDto uploadDocument(MultipartFile file, PdfUploadRequest request) {
        log.info("Starting PDF upload for file: {}", file.getOriginalFilename());

        try {
            // Validate file
            validatePdfFile(file);

            // Store file
            String filePath = fileStorageService.storeFile(file);

            // Extract PDF metadata
            int pageCount = PdfUtils.getPageCount(file);

            // Create entity
            PdfDocument document = new PdfDocument();
            document.setTitle(request.getTitle());
            document.setFilename(file.getOriginalFilename());
            document.setFilePath(filePath);
            document.setFileSize(file.getSize());
            document.setPageCount(pageCount);
            document.setProductCode(request.getProductCode());
            document.setEdition(request.getEdition());
            document.setPublicationDate(request.getPublicationDate());
            document.setNotes(request.getNotes());
            document.setContentType(file.getContentType());
            document.setCreatedBy(request.getCreatedBy() != null ? request.getCreatedBy() : "system");
            document.setIsActive(true);

            // Save to database
            document = repository.save(document);

            log.info("Successfully uploaded PDF with ID: {}", document.getId());
            return convertToDto(document);

        } catch (Exception e) {
            log.error("Error uploading PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload PDF: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PdfDocumentDto> getAllDocuments() {
        log.debug("Fetching all active documents");
        return repository.findByIsActiveTrueOrderByUploadDateDesc()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PdfDocumentDto> getDocumentById(Long id) {
        log.debug("Fetching document by ID: {}", id);
        return repository.findByIdAndIsActiveTrue(id)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PdfDocumentDto> searchDocuments(String searchQuery) {
        log.debug("Searching documents with query: {}", searchQuery);
        return repository.searchDocuments(searchQuery)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public PdfDocumentDto updateDocument(Long id, PdfUploadRequest request) {
        log.info("Updating document with ID: {}", id);

        PdfDocument document = repository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + id));

        // Update fields
        document.setTitle(request.getTitle());
        document.setProductCode(request.getProductCode());
        document.setEdition(request.getEdition());
        document.setPublicationDate(request.getPublicationDate());
        document.setNotes(request.getNotes());
        document.setUpdatedDate(LocalDateTime.now());

        document = repository.save(document);

        log.info("Successfully updated document with ID: {}", id);
        return convertToDto(document);
    }

    @Override
    public boolean deleteDocument(Long id) {
        log.info("Deleting document with ID: {}", id);

        Optional<PdfDocument> documentOpt = repository.findByIdAndIsActiveTrue(id);
        if (documentOpt.isPresent()) {
            PdfDocument document = documentOpt.get();

            // Soft delete
            document.setIsActive(false);
            document.setUpdatedDate(LocalDateTime.now());
            repository.save(document);

            // Delete file from storage
            try {
                fileStorageService.deleteFile(document.getFilePath());
            } catch (Exception e) {
                log.warn("Failed to delete file from storage: {}", e.getMessage());
            }

            log.info("Successfully deleted document with ID: {}", id);
            return true;
        }

        log.warn("Document not found for deletion with ID: {}", id);
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public Resource getDocumentFile(Long id) {
        log.debug("Getting file resource for document ID: {}", id);

        PdfDocument document = repository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + id));

        return fileStorageService.loadFileAsResource(document.getFilePath());
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadDocument(Long id) {
        log.debug("Downloading document with ID: {}", id);

        // For now, return the same as getDocumentFile
        // In the future, this could apply watermarks or other processing
        return getDocumentFile(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long getDocumentCount() {
        return repository.countByIsActiveTrue();
    }

    private void validatePdfFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        if (!"application/pdf".equals(file.getContentType())) {
            throw new RuntimeException("File must be a PDF");
        }

        // Check file size (50MB limit)
        if (file.getSize() > 50 * 1024 * 1024) {
            throw new RuntimeException("File size must not exceed 50MB");
        }
    }

    private PdfDocumentDto convertToDto(PdfDocument document) {
        PdfDocumentDto dto = new PdfDocumentDto();
        dto.setId(document.getId());
        dto.setTitle(document.getTitle());
        dto.setFilename(document.getFilename());
        dto.setFileSize(document.getFileSize());
        dto.setPageCount(document.getPageCount());
        dto.setProductCode(document.getProductCode());
        dto.setEdition(document.getEdition());
        dto.setPublicationDate(document.getPublicationDate());
        dto.setNotes(document.getNotes());
        dto.setContentType(document.getContentType());
        dto.setUploadDate(document.getUploadDate());
        dto.setCreatedBy(document.getCreatedBy());
        dto.setUpdatedDate(document.getUpdatedDate());
        return dto;
    }
}