package com.claritycheck.Backend.S3;

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

    public S3Controller(S3Service service) {
        this.service = service;
    }

    // 1. LIST FILES (Returns a List, which is valid JSON array)
    @GetMapping("/files")
    public ResponseEntity<List<String>> listFiles(@AuthenticationPrincipal OAuth2User principal) {
        String userEmail = principal.getAttribute("email");
        return ResponseEntity.ok(service.listFiles(userEmail));
    }

    // 2. UPLOAD (FIXED: Returns JSON Map)
    @PostMapping("/upload")
    public ResponseEntity<Object> upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal OAuth2User principal) throws IOException {

        String userEmail = principal.getAttribute("email");

        // Call service (which now returns the Analysis Object, not a String)
        Object analysisResult = service.upload(file, userEmail);

        // Return the analysis directly to the frontend
        return ResponseEntity.ok(analysisResult);
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