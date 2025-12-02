package com.claritycheck.Backend.S3;

import com.claritycheck.Backend.model.BiasReport;
import com.claritycheck.Backend.service.AggregatorService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections; // <--- Import this
import java.util.Map;         // <--- Import this
import java.util.List;

@RestController
@RequestMapping("/s3")
public class S3Controller {

    private final S3Service service;
    private final AggregatorService aggregatorService;

    public S3Controller(S3Service service, AggregatorService aggregatorService) {
        this.service = service;
        this.aggregatorService = aggregatorService;
    }

    // 1. LIST FILES (Returns a List, which is valid JSON array)
    @GetMapping("/files")
    public ResponseEntity<List<String>> listFiles(@AuthenticationPrincipal OAuth2User principal) {
        String userEmail = principal.getAttribute("email");
        return ResponseEntity.ok(service.listFiles(userEmail));
    }

    // 2. UPLOAD (FIXED: Returns JSON Map)
    @PostMapping("/upload")
    public ResponseEntity<BiasReport> upload( // Return the Standard BiasReport
                                              @RequestParam("file") MultipartFile file,
                                              @AuthenticationPrincipal OAuth2User principal) throws IOException {

        String userEmail = principal.getAttribute("email");

        // 1. Get the "Smart Summary" from S3Service + Python
        String filteredText = service.upload(file, userEmail);

        // 2. Feed it into your existing Chat Logic
        // This makes the response identical to the /api/chat endpoint!
        BiasReport report = aggregatorService.analyzeAll(filteredText);

        return ResponseEntity.ok(report);
    }

    // 3. DOWNLOAD (No changes needed, returns binary stream)
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> download(
            @PathVariable String fileName,
            @AuthenticationPrincipal OAuth2User principal) throws IOException {

        String userEmail = principal.getAttribute("email");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(new InputStreamResource(service.download(fileName, userEmail)));
    }

    // 4. DELETE (FIXED: Returns JSON Map)
    @DeleteMapping("/delete/{fileName}")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable String fileName,
            @AuthenticationPrincipal OAuth2User principal) {

        String userEmail = principal.getAttribute("email");
        String resultMsg = service.delete(fileName, userEmail);

        // WRAP IN JSON: { "message": "Deleted..." }
        return ResponseEntity.ok(Collections.singletonMap("message", resultMsg));
    }
}