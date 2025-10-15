//package com.example.demoPDF.service;
//
//import com.example.demoPDF.entity.Bookmark;
//import com.example.demoPDF.entity.PdfDocument;
//import com.example.demoPDF.repository.BookmarkRepository;
//import com.example.demoPDF.repository.PdfDocumentRepository;
//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import jakarta.servlet.http.HttpServletRequest;
//
//import java.io.File;
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//public class PdfService {
//
//    private static final String UPLOAD_DIR = "Z:\\USERDATA\\Abhay\\"; // Directory to save files
//
//    @Autowired
//    private PdfDocumentRepository pdfRepository;
//
//    @Autowired
//    private BookmarkRepository bookmarkRepository;
//
//    @Autowired
//    private WatermarkService watermarkService;
//
//    public PdfDocument uploadPdf(MultipartFile file, String title) throws IOException {
//        if (file.isEmpty()) {
//            throw new IllegalArgumentException("File cannot be empty");
//        }
//
//        if (!file.getContentType().equals("application/pdf")) {
//            throw new IllegalArgumentException("File must be a PDF");
//        }
//
//        // Ensure upload directory exists
//        File dir = new File(UPLOAD_DIR);
//        if (!dir.exists()) {
//            dir.mkdirs();
//        }
//
//        // Save file on server
//        String filePath = UPLOAD_DIR + file.getOriginalFilename();
//        File destination = new File(filePath);
//        file.transferTo(destination);
//
//        // Get page count from saved file
//        int pageCount = getPageCount(destination);
//
//        // Save only metadata + file path in DB
//        PdfDocument pdfDocument = new PdfDocument(
//                title != null ? title : file.getOriginalFilename(),
//                file.getOriginalFilename(),
//                filePath,
//                file.getSize(),
//                file.getContentType()
//        );
//        pdfDocument.setPageCount(pageCount);
//
//        return pdfRepository.save(pdfDocument);
//    }
//
//    public List<PdfDocument> getAllDocuments() {
//        List<Object[]> results = pdfRepository.findAllWithoutFileData(); // adjust query later
//        return results.stream().map(row -> {
//            PdfDocument doc = new PdfDocument();
//            doc.setId((Long) row[0]);
//            doc.setTitle((String) row[1]);
//            doc.setFilename((String) row[2]);
//            doc.setFileSize((Long) row[3]);
//            doc.setPageCount((Integer) row[4]);
//            doc.setContentType((String) row[5]);
//            doc.setUploadDate((LocalDateTime) row[6]);
//            return doc;
//        }).collect(Collectors.toList());
//    }
//
//    public Optional<PdfDocument> getDocumentById(Long id) {
//        return pdfRepository.findById(id);
//    }
//
//    public byte[] getDocumentData(Long id) throws IOException {
//        Optional<PdfDocument> docOpt = pdfRepository.findById(id);
//        if (docOpt.isEmpty()) {
//            throw new IllegalArgumentException("Document not found");
//        }
//
//        PdfDocument doc = docOpt.get();
//        File file = new File(doc.getFilePath());
//        if (!file.exists()) {
//            throw new IllegalStateException("File not found on server");
//        }
//
//        return java.nio.file.Files.readAllBytes(file.toPath());
//    }
//
//    public byte[] getWatermarkedDocument(Long id) throws IOException {
//        Optional<PdfDocument> documentOpt = getDocumentById(id);
//        if (documentOpt.isEmpty()) {
//            throw new IllegalArgumentException("Document not found");
//        }
//
//        PdfDocument document = documentOpt.get();
//        byte[] originalData = getDocumentData(id);
//
//        String watermarkText = "CONFIDENTIAL - " + document.getTitle();
//        return watermarkService.addWatermarkToPdf(originalData, watermarkText);
//    }
//
//    public List<PdfDocument> searchDocuments(String query) {
//        return pdfRepository.findByTitleContainingIgnoreCase(query);
//    }
//
//    public void deleteDocument(Long id) {
//        Optional<PdfDocument> docOpt = pdfRepository.findById(id);
//        if (docOpt.isPresent()) {
//            // delete file from server
//            File file = new File(docOpt.get().getFilePath());
//            if (file.exists()) {
//                file.delete();
//            }
//        }
//        pdfRepository.deleteById(id);
//    }
//
//    // Bookmark functionality
//    public Bookmark addBookmark(Long documentId, String userId, Integer pageNumber, String bookmarkName) {
//        Optional<PdfDocument> documentOpt = getDocumentById(documentId);
//        if (documentOpt.isEmpty()) {
//            throw new IllegalArgumentException("Document not found");
//        }
//
//        PdfDocument document = documentOpt.get();
//
//        // Check if bookmark already exists for this user and document
//        Optional<Bookmark> existingBookmark = bookmarkRepository.findByUserIdAndDocumentId(userId, documentId);
//        if (existingBookmark.isPresent()) {
//            // Update existing bookmark
//            Bookmark bookmark = existingBookmark.get();
//            bookmark.setPageNumber(pageNumber);
//            bookmark.setBookmarkName(bookmarkName != null ? bookmarkName : "Page " + pageNumber);
//            bookmark.setCreatedDate(LocalDateTime.now());
//            return bookmarkRepository.save(bookmark);
//        } else {
//            // Create new bookmark
//            Bookmark bookmark = new Bookmark(
//                    userId,
//                    document,
//                    pageNumber,
//                    bookmarkName != null ? bookmarkName : "Page " + pageNumber
//            );
//            return bookmarkRepository.save(bookmark);
//        }
//    }
//
//    public List<Bookmark> getUserBookmarks(String userId) {
//        return bookmarkRepository.findByUserIdOrderByCreatedDateDesc(userId);
//    }
//
//    public Optional<Bookmark> getUserBookmarkForDocument(String userId, Long documentId) {
//        return bookmarkRepository.findByUserIdAndDocumentId(userId, documentId);
//    }
//
//    public void removeBookmark(String userId, Long documentId) {
//        bookmarkRepository.deleteByUserIdAndPdfDocumentId(userId, documentId);
//    }
//
//    // Utility method to get user identifier (IP address for simplicity)
//    public String getUserIdentifier(HttpServletRequest request) {
//        String xForwardedFor = request.getHeader("X-Forwarded-For");
//        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
//            return xForwardedFor.split(",")[0].trim();
//        }
//
//        String xRealIp = request.getHeader("X-Real-IP");
//        if (xRealIp != null && !xRealIp.isEmpty()) {
//            return xRealIp;
//        }
//
//        return request.getRemoteAddr();
//    }
//
//    // Utility method to count pages from file
//    private int getPageCount(File file) throws IOException {
//        try (PDDocument document = PDDocument.load(file)) {
//            return document.getNumberOfPages();
//        }
//    }
//
//
//}
