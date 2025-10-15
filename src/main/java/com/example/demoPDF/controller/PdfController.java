//package com.example.demoPDF.controller;
//
//import com.example.demoPDF.entity.Bookmark;
//import com.example.demoPDF.entity.PdfDocument;
//import com.example.demoPDF.service.PdfService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import jakarta.servlet.http.HttpServletRequest;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.util.List;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/pdf")
//@CrossOrigin(origins = "*")
//public class PdfController {
//
//    @Autowired
//    private PdfService pdfService;
//
//    // Upload and save PDF to server + DB path
//    @PostMapping("/upload")
//    public ResponseEntity<?> uploadPdf(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam(value = "title", required = false) String title) {
//        try {
//            PdfDocument savedDocument = pdfService.uploadPdf(file, title);
//            return ResponseEntity.ok().body(savedDocument);
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Error uploading file: " + e.getMessage());
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
//
//    @GetMapping("/documents")
//    public ResponseEntity<List<PdfDocument>> getAllDocuments() {
//        List<PdfDocument> documents = pdfService.getAllDocuments();
//        return ResponseEntity.ok(documents);
//    }
//
//    @GetMapping("/document/{id}")
//    public ResponseEntity<PdfDocument> getDocument(@PathVariable Long id) {
//        Optional<PdfDocument> document = pdfService.getDocumentById(id);
//        return document.map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    // View PDF directly from server
//    @GetMapping("/view/{id}")
//    public ResponseEntity<byte[]> viewPdf(@PathVariable Long id) {
//        try {
//            Optional<PdfDocument> documentOpt = pdfService.getDocumentById(id);
//            if (documentOpt.isEmpty()) {
//                return ResponseEntity.notFound().build();
//            }
//
//            PdfDocument document = documentOpt.get();
//            File file = new File(document.getFilePath());
//            if (!file.exists()) {
//                return ResponseEntity.notFound().build();
//            }
//
//            byte[] fileData = Files.readAllBytes(file.toPath());
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_PDF);
//            headers.setContentDispositionFormData("inline", document.getFilename());
//            headers.setContentLength(fileData.length);
//            headers.add("Cache-Control", "no-cache, no-store, must-revalidate, private");
//            headers.add("Pragma", "no-cache");
//            headers.add("Expires", "0");
//
//            return ResponseEntity.ok().headers(headers).body(fileData);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    // Download PDF with watermark
//    @GetMapping("/download/{id}")
//    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
//        try {
//            Optional<PdfDocument> documentOpt = pdfService.getDocumentById(id);
//            if (documentOpt.isEmpty()) {
//                return ResponseEntity.notFound().build();
//            }
//
//            PdfDocument document = documentOpt.get();
//            byte[] watermarkedData = pdfService.getWatermarkedDocument(id);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_PDF);
//            headers.setContentDispositionFormData("attachment", "watermarked_" + document.getFilename());
//            headers.setContentLength(watermarkedData.length);
//
//            return ResponseEntity.ok().headers(headers).body(watermarkedData);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Error creating watermarked PDF".getBytes());
//        }
//    }
//
//    // --- Bookmarks (unchanged) ---
//    @PostMapping("/bookmark/{documentId}")
//    public ResponseEntity<?> addBookmark(
//            @PathVariable Long documentId,
//            @RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
//            @RequestParam(value = "name", required = false) String bookmarkName,
//            HttpServletRequest request) {
//        try {
//            String userId = pdfService.getUserIdentifier(request);
//            Bookmark bookmark = pdfService.addBookmark(documentId, userId, pageNumber, bookmarkName);
//            return ResponseEntity.ok().body(bookmark);
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Error creating bookmark: " + e.getMessage());
//        }
//    }
//
//    @GetMapping("/bookmarks")
//    public ResponseEntity<List<Bookmark>> getUserBookmarks(HttpServletRequest request) {
//        try {
//            String userId = pdfService.getUserIdentifier(request);
//            List<Bookmark> bookmarks = pdfService.getUserBookmarks(userId);
//            return ResponseEntity.ok(bookmarks);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    @GetMapping("/bookmark/{documentId}")
//    public ResponseEntity<Bookmark> getBookmarkForDocument(
//            @PathVariable Long documentId,
//            HttpServletRequest request) {
//        try {
//            String userId = pdfService.getUserIdentifier(request);
//            Optional<Bookmark> bookmark = pdfService.getUserBookmarkForDocument(userId, documentId);
//            return bookmark.map(ResponseEntity::ok)
//                    .orElse(ResponseEntity.notFound().build());
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    @DeleteMapping("/bookmark/{documentId}")
//    public ResponseEntity<Void> removeBookmark(
//            @PathVariable Long documentId,
//            HttpServletRequest request) {
//        try {
//            String userId = pdfService.getUserIdentifier(request);
//            pdfService.removeBookmark(userId, documentId);
//            return ResponseEntity.ok().build();
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    @GetMapping("/search")
//    public ResponseEntity<List<PdfDocument>> searchDocuments(@RequestParam String query) {
//        List<PdfDocument> documents = pdfService.searchDocuments(query);
//        return ResponseEntity.ok(documents);
//    }
//
//    @DeleteMapping("/document/{id}")
//    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
//        try {
//            pdfService.deleteDocument(id);
//            return ResponseEntity.ok().build();
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//}
