package com.claritycheck.Backend.S3;

import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class S3Service {

    private final S3Template s3Template;
    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    public S3Service(S3Template s3Template, S3Client s3Client) {
        this.s3Template = s3Template;
        this.s3Client = s3Client;
    }

    /**
     * Uploads a file with Quota Check (Max 3 files per user)
     */
    public String upload(MultipartFile file, String userEmail) throws IOException {
        String userPrefix = userEmail + "/";

        // 1. CHECK QUOTA
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(userPrefix)
                .maxKeys(4)
                .build();

        ListObjectsV2Response result = s3Client.listObjectsV2(listRequest);
        int currentCount = result.keyCount();

        if (currentCount >= 3) {
            throw new RuntimeException("Upload limit reached! User " + userEmail + " already has 3 files.");
        }

        // 2. UPLOAD
        String key = userPrefix + file.getOriginalFilename();
        s3Template.upload(bucketName, key, file.getInputStream());

        return "Uploaded " + key;
    }

    /**
     * NEW METHOD: Lists files for a specific user only
     */
    public List<String> listFiles(String userEmail) {
        String userPrefix = userEmail + "/";

        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(userPrefix) // Only look in this user's folder
                .build();

        return s3Client.listObjectsV2(request).contents().stream()
                .map(S3Object::key)
                // Remove the "email/" prefix so frontend just sees "resume.pdf"
                .map(key -> key.replace(userPrefix, ""))
                .collect(Collectors.toList());
    }

    /**
     * UPDATED: Takes email to construct the secure path
     */
    public InputStream download(String fileName, String userEmail) throws IOException {
        String fullPath = userEmail + "/" + fileName;
        S3Resource s3Resource = s3Template.download(bucketName, fullPath);
        return s3Resource.getInputStream();
    }

    /**
     * UPDATED: Takes email to delete from the secure path
     */
    public String delete(String fileName, String userEmail) {
        String fullPath = userEmail + "/" + fileName;
        s3Template.deleteObject(bucketName, fullPath);
        return "Deleted " + fileName;
    }
}