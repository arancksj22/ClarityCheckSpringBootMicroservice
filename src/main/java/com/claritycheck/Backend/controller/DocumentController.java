package com.claritycheck.Backend.controller;

import com.claritycheck.Backend.model.UserDocument;
import com.claritycheck.Backend.service.DocumentService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@Validated
@ConditionalOnBean(DocumentService.class)
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    private String userId(Jwt jwt, String debugHeader) {
        if (jwt != null) return jwt.getSubject();
        if (debugHeader != null && !debugHeader.isBlank()) return debugHeader;
        return "dev-user"; // dev fallback when security is disabled
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> upload(
            @RequestHeader(value = "X-Debug-User", required = false) String debugUser,
            @AuthenticationPrincipal Jwt jwt,
            @RequestPart("file") MultipartFile file) {
        UserDocument doc = service.upload(userId(jwt, debugUser), file);
        return ResponseEntity.ok(Map.of(
            "id", doc.getId(),
            "fileName", doc.getFileName(),
            "s3Key", doc.getS3Key(),
            "sizeBytes", doc.getSizeBytes()
        ));
    }

    @GetMapping
    public ResponseEntity<List<UserDocument>> list(
            @RequestHeader(value = "X-Debug-User", required = false) String debugUser,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.list(userId(jwt, debugUser)));
    }

    @GetMapping("/{id}/download-url")
    public ResponseEntity<Map<String, String>> downloadUrl(
            @RequestHeader(value = "X-Debug-User", required = false) String debugUser,
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @RequestParam(name = "ttlSeconds", defaultValue = "900") long ttlSeconds) {
        URI url = service.presignedDownloadUrl(userId(jwt, debugUser), id, Duration.ofSeconds(ttlSeconds));
        return ResponseEntity.ok(Map.of("url", url.toString()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestHeader(value = "X-Debug-User", required = false) String debugUser,
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {
        service.delete(userId(jwt, debugUser), id);
        return ResponseEntity.noContent().build();
    }
}

