package com.claritycheck.Backend.service;

import com.claritycheck.Backend.model.UserDocument;
import com.claritycheck.Backend.repository.UserDocumentRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnBean({S3Client.class, S3Presigner.class, UserDocumentRepository.class})
public class DocumentService {

    private static final int MAX_DOCS_PER_USER = 3;

    private final String bucket;
    private final S3Client s3;
    private final S3Presigner presigner;
    private final UserDocumentRepository repo;

    public DocumentService(
            UserDocumentRepository repo,
            S3Client s3,
            S3Presigner presigner,
            org.springframework.core.env.Environment env) {
        this.repo = repo;
        this.s3 = s3;
        this.presigner = presigner;
        this.bucket = env.getProperty("app.s3.bucket");
        if (this.bucket == null || this.bucket.isBlank()) {
            throw new IllegalStateException("app.s3.bucket must be configured when S3 is enabled");
        }
    }

    public UserDocument upload(String userId, MultipartFile file) {
        if (repo.countByUserId(userId) >= MAX_DOCS_PER_USER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Max 3 PDFs reached");
        }
        if (file.isEmpty() || (file.getContentType() != null && !file.getContentType().toLowerCase().contains("pdf"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only non-empty PDF files are allowed");
        }

        String key = userId + "/" + UUID.randomUUID() + ".pdf";

        try {
            PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("application/pdf")
                .build();

            s3.putObject(putReq, RequestBody.fromBytes(file.getBytes()));

            UserDocument doc = new UserDocument();
            doc.setUserId(userId);
            doc.setS3Key(key);
            doc.setFileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.pdf");
            doc.setContentType("application/pdf");
            doc.setSizeBytes(file.getSize());
            return repo.save(doc);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed", e);
        }
    }

    public List<UserDocument> list(String userId) {
        return repo.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    public URI presignedDownloadUrl(String userId, UUID id, Duration ttl) {
        UserDocument doc = repo.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found"));

        GetObjectRequest getReq = GetObjectRequest.builder()
            .bucket(bucket)
            .key(doc.getS3Key())
            .build();

        PresignedGetObjectRequest pre = presigner.presignGetObject(
            GetObjectPresignRequest.builder()
                .signatureDuration(ttl != null ? ttl : Duration.ofMinutes(15))
                .getObjectRequest(getReq)
                .build()
        );
        return pre.url().toURI();
    }

    public void delete(String userId, UUID id) {
        UserDocument doc = repo.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found"));
        try {
            s3.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(doc.getS3Key())
                .build());
        } catch (S3Exception e) {
            // continue
        }
        repo.delete(doc);
    }
}
