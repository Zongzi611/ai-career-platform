package com.careersail.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Slf4j
public class FileTextExtractor {

    public static String extract(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) return "";

        String lower = filename.toLowerCase();
        try {
            if (lower.endsWith(".pdf")) {
                return extractPdf(file);
            } else if (lower.endsWith(".docx")) {
                return extractDocx(file);
            } else if (lower.endsWith(".txt") || lower.endsWith(".md") || lower.endsWith(".java")
                    || lower.endsWith(".py") || lower.endsWith(".html") || lower.endsWith(".css")
                    || lower.endsWith(".js") || lower.endsWith(".json") || lower.endsWith(".xml")
                    || lower.endsWith(".yml") || lower.endsWith(".yaml") || lower.endsWith(".csv")) {
                return extractText(file);
            } else {
                // Try as plain text, fall back to error message
                return extractText(file);
            }
        } catch (Exception e) {
            log.error("Failed to extract text from {}: {}", filename, e.getMessage());
            return "[文件解析失败: " + e.getMessage() + "]";
        }
    }

    private static String extractPdf(MultipartFile file) throws Exception {
        var doc = Loader.loadPDF(file.getBytes());
        var stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        String text = stripper.getText(doc);
        doc.close();
        return text;
    }

    private static String extractDocx(MultipartFile file) throws Exception {
        var doc = new XWPFDocument(file.getInputStream());
        var extractor = new XWPFWordExtractor(doc);
        String text = extractor.getText();
        extractor.close();
        doc.close();
        return text;
    }

    private static String extractText(MultipartFile file) throws Exception {
        var sb = new StringBuilder();
        try (var reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }
}
