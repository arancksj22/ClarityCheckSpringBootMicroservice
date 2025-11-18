package com.claritycheck.Backend.S3;

import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

import java.io.IOException;
import java.io.InputStream;

@Service
public class S3Service {

    private final S3Template s3Template;
    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    // Constructor Injection for S3Template (high-level) and S3Client (low-level, for counting)
    public S3Service(S3Template s3Template, S3Client s3Client) {
        this.s3Template = s3Template;
        this.s3Client = s3Client;
    }

    /**
     * Uploads a file to S3, enforcing a limit of 3 files per user.
     * Files are stored under a user-specific prefix (virtual folder).
     */
    public String upload(MultipartFile file, String userEmail) throws IOException {

        String userPrefix = userEmail + "/";

        // 2. CHECK QUOTA: Count objects with that prefix using the S3Client
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(userPrefix)
                .maxKeys(4) // Only fetch up to 4 keys (limit + 1) to save bandwidth
                .build();

        ListObjectsV2Response result = s3Client.listObjectsV2(listRequest);
        int currentCount = result.keyCount();

        if (currentCount >= 3) {
            throw new RuntimeException("Upload limit reached! User " + userEmail + " already has 3 files.");
        }

        String key = userPrefix + file.getOriginalFilename();
        s3Template.upload(bucketName, key, file.getInputStream());

        return "Uploaded " + key;
    }

    public InputStream download(String key) throws IOException {
        // NOTE: This assumes the key passed to the service already includes the userPrefix if needed
        S3Resource s3Resource = s3Template.download(bucketName, key);
        return s3Resource.getInputStream();
    }

    /**
     * Deletes a file from the user's S3 virtual folder.
     */
    public String delete(String key) {
        // NOTE: This assumes the key passed to the service already includes the userPrefix if needed
        s3Template.deleteObject(bucketName, key);
        return "Deleted " + key;
    }
}