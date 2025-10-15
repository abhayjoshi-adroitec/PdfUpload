//package com.example.demoPDF.service;
//
//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.pdmodel.PDPage;
//import org.apache.pdfbox.pdmodel.PDPageContentStream;
//import org.apache.pdfbox.pdmodel.font.PDType1Font;
//import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
//import org.apache.pdfbox.util.Matrix;
//import org.springframework.stereotype.Service;
//
//import java.awt.*;
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//
//@Service
//public class WatermarkService {
//
//    public byte[] addWatermarkToPdf(byte[] pdfData, String watermarkText) throws IOException {
//        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfData);
//             PDDocument document = PDDocument.load(inputStream);
//             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
//
//            // Create watermark text with timestamp
//            String fullWatermarkText = watermarkText + " - Downloaded: " +
//                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//
//            // Add watermark to each page
//            for (PDPage page : document.getPages()) {
//                addWatermarkToPage(document, page, fullWatermarkText);
//            }
//
//            document.save(outputStream);
//            return outputStream.toByteArray();
//        }
//    }
//
//    private void addWatermarkToPage(PDDocument document, PDPage page, String watermarkText) throws IOException {
//        PDPageContentStream contentStream = new PDPageContentStream(
//                document, page, PDPageContentStream.AppendMode.APPEND, true, true);
//
//        try {
//            // Set up graphics state for transparency
//            PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
//            graphicsState.setNonStrokingAlphaConstant(0.3f);
//            contentStream.setGraphicsStateParameters(graphicsState);
//
//            // Get page dimensions
//            float pageWidth = page.getMediaBox().getWidth();
//            float pageHeight = page.getMediaBox().getHeight();
//
//            // Set font and color
//            contentStream.setNonStrokingColor(Color.RED);
//            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
//
//            // Calculate text width for centering
//            float textWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(watermarkText) / 1000 * 24;
//
//            // Position for diagonal watermark
//            float x = (pageWidth - textWidth) / 2;
//            float y = pageHeight / 2;
//
//            // Start text
//            contentStream.beginText();
//
//            // Apply rotation and translation for diagonal text
//            Matrix matrix = new Matrix();
//            matrix.translate(x, y);
//            matrix.rotate(Math.toRadians(-45)); // 45-degree rotation
//            contentStream.setTextMatrix(matrix);
//
//            // Add the watermark text
//            contentStream.showText(watermarkText);
//            contentStream.endText();
//
//            // Add additional smaller watermarks in corners
//            addCornerWatermarks(contentStream, pageWidth, pageHeight, "CONFIDENTIAL");
//
//        } finally {
//            contentStream.close();
//        }
//    }
//
//    private void addCornerWatermarks(PDPageContentStream contentStream, float pageWidth,
//                                     float pageHeight, String cornerText) throws IOException {
//
//        // Set smaller font for corner watermarks
//        contentStream.setFont(PDType1Font.HELVETICA, 10);
//        contentStream.setNonStrokingColor(Color.GRAY);
//
//        // Top-left corner
//        contentStream.beginText();
//        contentStream.newLineAtOffset(20, pageHeight - 30);
//        contentStream.showText(cornerText);
//        contentStream.endText();
//
//        // Top-right corner
//        float cornerTextWidth = PDType1Font.HELVETICA.getStringWidth(cornerText) / 1000 * 10;
//        contentStream.beginText();
//        contentStream.newLineAtOffset(pageWidth - cornerTextWidth - 20, pageHeight - 30);
//        contentStream.showText(cornerText);
//        contentStream.endText();
//
//        // Bottom-left corner
//        contentStream.beginText();
//        contentStream.newLineAtOffset(20, 20);
//        contentStream.showText(cornerText);
//        contentStream.endText();
//
//        // Bottom-right corner
//        contentStream.beginText();
//        contentStream.newLineAtOffset(pageWidth - cornerTextWidth - 20, 20);
//        contentStream.showText(cornerText);
//        contentStream.endText();
//
//        // Center watermark (faint)
//        PDExtendedGraphicsState faintState = new PDExtendedGraphicsState();
//        faintState.setNonStrokingAlphaConstant(0.1f);
//        contentStream.setGraphicsStateParameters(faintState);
//
//        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 72);
//        contentStream.setNonStrokingColor(Color.LIGHT_GRAY);
//
//        float centerTextWidth = PDType1Font.HELVETICA_BOLD.getStringWidth("DOWNLOAD COPY") / 1000 * 72;
//        contentStream.beginText();
//        Matrix centerMatrix = new Matrix();
//        centerMatrix.translate((pageWidth - centerTextWidth) / 2, pageHeight / 2 - 100);
//        centerMatrix.rotate(Math.toRadians(-45));
//        contentStream.setTextMatrix(centerMatrix);
//        contentStream.showText("DOWNLOAD COPY");
//        contentStream.endText();
//    }
//}