package com.example.demoPDF.service;

import com.example.demoPDF.dto.PdfDocumentDto;
import com.example.demoPDF.dto.PdfUploadRequest;
//import com.example.demoPDF.entity.PdfDocument;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface PdfDocumentService {

    PdfDocumentDto uploadDocument(MultipartFile file, PdfUploadRequest request);

    List<PdfDocumentDto> getAllDocuments();

    Optional<PdfDocumentDto> getDocumentById(Long id);

    List<PdfDocumentDto> searchDocuments(String searchQuery);

    PdfDocumentDto updateDocument(Long id, PdfUploadRequest request);

    boolean deleteDocument(Long id);

    Resource getDocumentFile(Long id);

    Resource downloadDocument(Long id);

    long getDocumentCount();
}
