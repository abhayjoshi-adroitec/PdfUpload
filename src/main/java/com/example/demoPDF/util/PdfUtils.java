// PdfUtils.java - Fixed version for PDFBox 3.x
package com.example.demoPDF.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class PdfUtils {

    public static int getPageCount(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            // Convert InputStream to byte array for PDFBox 3.x
            byte[] pdfBytes = inputStream.readAllBytes();

            try (PDDocument document = Loader.loadPDF(pdfBytes)) {
                int pageCount = document.getNumberOfPages();
                log.debug("PDF page count: {}", pageCount);
                return pageCount;
            }
        } catch (IOException e) {
            log.warn("Could not extract page count from PDF: {}", e.getMessage());
            return 0;
        }
    }

    public static boolean isPdfValid(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            byte[] pdfBytes = inputStream.readAllBytes();

            try (PDDocument document = Loader.loadPDF(pdfBytes)) {
                boolean isValid = document.getNumberOfPages() > 0;
                log.debug("PDF validation result: {}", isValid);
                return isValid;
            }
        } catch (IOException e) {
            log.warn("PDF validation failed: {}", e.getMessage());
            return false;
        }
    }

    public static String extractText(MultipartFile file, int maxPages) {
        try (InputStream inputStream = file.getInputStream()) {
            byte[] pdfBytes = inputStream.readAllBytes();

            try (PDDocument document = Loader.loadPDF(pdfBytes)) {
                PDFTextStripper textStripper = new PDFTextStripper();

                // Set the range of pages to extract (1-based indexing)
                int totalPages = document.getNumberOfPages();
                int pagesToExtract = Math.min(maxPages, totalPages);

                textStripper.setStartPage(1);
                textStripper.setEndPage(pagesToExtract);

                String extractedText = textStripper.getText(document);
                log.debug("Extracted text from {} pages", pagesToExtract);

                return extractedText;
            }
        } catch (IOException e) {
            log.warn("Could not extract text from PDF: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Extract text from first page only
     */
    public static String extractFirstPageText(MultipartFile file) {
        return extractText(file, 1);
    }

    /**
     * Get PDF metadata information
     */
    public static PdfMetadata getPdfMetadata(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            byte[] pdfBytes = inputStream.readAllBytes();

            try (PDDocument document = Loader.loadPDF(pdfBytes)) {
                PdfMetadata metadata = new PdfMetadata();
                metadata.setPageCount(document.getNumberOfPages());
                metadata.setVersion(document.getVersion());

                // Get document information if available
                if (document.getDocumentInformation() != null) {
                    var docInfo = document.getDocumentInformation();
                    metadata.setTitle(docInfo.getTitle());
                    metadata.setAuthor(docInfo.getAuthor());
                    metadata.setSubject(docInfo.getSubject());
                    metadata.setCreator(docInfo.getCreator());
                    metadata.setProducer(docInfo.getProducer());
                    metadata.setCreationDate(docInfo.getCreationDate());
                    metadata.setModificationDate(docInfo.getModificationDate());
                }

                return metadata;
            }
        } catch (IOException e) {
            log.warn("Could not extract PDF metadata: {}", e.getMessage());
            return new PdfMetadata();
        }
    }

    /**
     * Check if PDF is encrypted/password protected
     */
    public static boolean isPasswordProtected(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            byte[] pdfBytes = inputStream.readAllBytes();

            try (PDDocument document = Loader.loadPDF(pdfBytes)) {
                return document.isEncrypted();
            }
        } catch (IOException e) {
            log.warn("Could not check PDF encryption status: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get file size in a readable format
     */
    public static String getReadableFileSize(long bytes) {
        if (bytes <= 0) return "0 B";

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));

        return String.format("%.1f %s",
                bytes / Math.pow(1024, digitGroups),
                units[digitGroups]
        );
    }

    /**
     * Inner class to hold PDF metadata
     */
    public static class PdfMetadata {
        private int pageCount;
        private float version;
        private String title;
        private String author;
        private String subject;
        private String creator;
        private String producer;
        private java.util.Calendar creationDate;
        private java.util.Calendar modificationDate;

        // Constructors
        public PdfMetadata() {}

        // Getters and Setters
        public int getPageCount() { return pageCount; }
        public void setPageCount(int pageCount) { this.pageCount = pageCount; }

        public float getVersion() { return version; }
        public void setVersion(float version) { this.version = version; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }

        public String getCreator() { return creator; }
        public void setCreator(String creator) { this.creator = creator; }

        public String getProducer() { return producer; }
        public void setProducer(String producer) { this.producer = producer; }

        public java.util.Calendar getCreationDate() { return creationDate; }
        public void setCreationDate(java.util.Calendar creationDate) { this.creationDate = creationDate; }

        public java.util.Calendar getModificationDate() { return modificationDate; }
        public void setModificationDate(java.util.Calendar modificationDate) { this.modificationDate = modificationDate; }

        @Override
        public String toString() {
            return String.format("PdfMetadata{pageCount=%d, version=%.1f, title='%s', author='%s'}",
                    pageCount, version, title, author);
        }
    }
}